package com.rushikesh.fitscore.service;

import com.rushikesh.fitscore.model.ExtractedProfile;
import com.rushikesh.fitscore.model.ScoreBreakdown;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    @Test
    void partialSkillMatchAndMeetsExperience() {
        ExtractedProfile resume = new ExtractedProfile(
                List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Kafka", "Git", "Maven"), 4);
        ExtractedProfile jd = new ExtractedProfile(
                List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Kafka", "Git", "Maven",
                        "Kubernetes", "AWS", "Redis"), 4);

        ScoreBreakdown result = calculator.calculate(resume, jd);

        assertEquals(49.0, result.skillScore());
        assertEquals(30.0, result.experienceScore());
        assertEquals(79.0, result.totalScore());
        assertEquals(List.of("Kubernetes", "AWS", "Redis"), result.missingSkills());
    }

    @Test
    void skillNamesAreNormalized() {
        ExtractedProfile resume = new ExtractedProfile(List.of("SpringBoot", "postgre-sql"), 2);
        ExtractedProfile jd = new ExtractedProfile(List.of("Spring Boot", "PostgreSQL"), 2);

        ScoreBreakdown result = calculator.calculate(resume, jd);

        assertEquals(70.0, result.skillScore());
        assertEquals(List.of("Spring Boot", "PostgreSQL"), result.matchedSkills());
    }

    @Test
    void experienceIsProportionalWhenBelowRequirement() {
        ExtractedProfile resume = new ExtractedProfile(List.of("Java"), 2);
        ExtractedProfile jd = new ExtractedProfile(List.of("Java"), 4);

        ScoreBreakdown result = calculator.calculate(resume, jd);

        assertEquals(15.0, result.experienceScore());
        assertEquals(85.0, result.totalScore());
    }

    @Test
    void noRequiredSkillsGivesZeroSkillScore() {
        ExtractedProfile resume = new ExtractedProfile(List.of("Java"), 3);
        ExtractedProfile jd = new ExtractedProfile(List.of(), 0);

        ScoreBreakdown result = calculator.calculate(resume, jd);

        assertEquals(0.0, result.skillScore());
        assertEquals(30.0, result.experienceScore());
    }
}
