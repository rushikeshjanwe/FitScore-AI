package com.rushikesh.fitscore.model;

import java.util.List;

/**
 * What the LLM extracts from a resume or a job description.
 * For a resume: skills the candidate has, total years of experience.
 * For a JD: skills required, years of experience required.
 */
public record ExtractedProfile(List<String> skills, int yearsOfExperience) {
}
