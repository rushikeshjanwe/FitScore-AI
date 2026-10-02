package com.rushikesh.fitscore.model;

import java.time.Instant;
import java.util.List;

/** What the API returns to the client. */
public record ScoreResponse(Long id,
                            String resumeFileName,
                            double totalScore,
                            double skillScore,
                            double experienceScore,
                            List<String> matchedSkills,
                            List<String> missingSkills,
                            Instant createdAt) {

    public static ScoreResponse from(FitScoreResult r) {
        return new ScoreResponse(r.getId(), r.getResumeFileName(), r.getTotalScore(),
                r.getSkillScore(), r.getExperienceScore(),
                List.copyOf(r.getMatchedSkills()), List.copyOf(r.getMissingSkills()),
                r.getCreatedAt());
    }
}
