package com.rushikesh.fitscore.service;

import com.rushikesh.fitscore.model.ExtractedProfile;
import com.rushikesh.fitscore.model.ScoreBreakdown;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Plain Java scoring, no AI involved, so the same input always gives the same score.
 *
 * Skill score      (max 70) = matched JD skills / total JD skills * 70
 * Experience score (max 30) = full 30 if resume years >= required years, otherwise proportional
 */
@Component
public class ScoreCalculator {

    static final double SKILL_WEIGHT = 70.0;
    static final double EXPERIENCE_WEIGHT = 30.0;

    public ScoreBreakdown calculate(ExtractedProfile resume, ExtractedProfile jd) {
        Set<String> resumeSkills = resume.skills().stream()
                .map(ScoreCalculator::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // key = normalized name, value = original name (so we can show the JD's wording)
        Map<String, String> jdSkills = new LinkedHashMap<>();
        for (String skill : jd.skills()) {
            String key = normalize(skill);
            if (!key.isEmpty()) {
                jdSkills.putIfAbsent(key, skill.trim());
            }
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        jdSkills.forEach((key, original) -> {
            if (resumeSkills.contains(key)) {
                matched.add(original);
            } else {
                missing.add(original);
            }
        });

        double skillScore = jdSkills.isEmpty()
                ? 0.0
                : (double) matched.size() / jdSkills.size() * SKILL_WEIGHT;

        double experienceScore = experienceScore(resume.yearsOfExperience(), jd.yearsOfExperience());

        double total = round(skillScore + experienceScore);
        return new ScoreBreakdown(total, round(skillScore), round(experienceScore), matched, missing);
    }

    private double experienceScore(int resumeYears, int requiredYears) {
        if (requiredYears <= 0 || resumeYears >= requiredYears) {
            return EXPERIENCE_WEIGHT;
        }
        return Math.max(resumeYears, 0) / (double) requiredYears * EXPERIENCE_WEIGHT;
    }

    /** "Spring Boot", "SpringBoot" and "spring-boot" all become "springboot". */
    static String normalize(String skill) {
        if (skill == null) {
            return "";
        }
        return skill.toLowerCase().replaceAll("[\\s\\-_.]", "");
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
