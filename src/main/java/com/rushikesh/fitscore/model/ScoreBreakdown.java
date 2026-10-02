package com.rushikesh.fitscore.model;

import java.util.List;

/** Result of the scoring logic, before it is saved. */
public record ScoreBreakdown(double totalScore,
                             double skillScore,
                             double experienceScore,
                             List<String> matchedSkills,
                             List<String> missingSkills) {
}
