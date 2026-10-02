package com.rushikesh.fitscore.model;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Index on created_at supports the history query (newest first).
 * The skill collections are indexed on result_id so loading a result's skills is not a full scan.
 */
@Entity
@Table(name = "fit_score_results",
        indexes = @Index(name = "idx_fit_score_created_at", columnList = "created_at"))
public class FitScoreResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resumeFileName;

    private double totalScore;
    private double skillScore;
    private double experienceScore;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "matched_skills",
            joinColumns = @JoinColumn(name = "result_id"),
            indexes = @Index(name = "idx_matched_skills_result", columnList = "result_id"))
    @Column(name = "skill")
    @BatchSize(size = 20)
    private List<String> matchedSkills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "missing_skills",
            joinColumns = @JoinColumn(name = "result_id"),
            indexes = @Index(name = "idx_missing_skills_result", columnList = "result_id"))
    @Column(name = "skill")
    @BatchSize(size = 20)
    private List<String> missingSkills = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected FitScoreResult() {
        // required by JPA
    }

    public FitScoreResult(String resumeFileName, double totalScore, double skillScore,
                          double experienceScore, List<String> matchedSkills, List<String> missingSkills) {
        this.resumeFileName = resumeFileName;
        this.totalScore = totalScore;
        this.skillScore = skillScore;
        this.experienceScore = experienceScore;
        this.matchedSkills = new ArrayList<>(matchedSkills);
        this.missingSkills = new ArrayList<>(missingSkills);
    }

    public Long getId() { return id; }
    public String getResumeFileName() { return resumeFileName; }
    public double getTotalScore() { return totalScore; }
    public double getSkillScore() { return skillScore; }
    public double getExperienceScore() { return experienceScore; }
    public List<String> getMatchedSkills() { return matchedSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
    public Instant getCreatedAt() { return createdAt; }
}
