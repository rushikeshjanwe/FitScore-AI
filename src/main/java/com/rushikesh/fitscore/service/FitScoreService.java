package com.rushikesh.fitscore.service;

import com.rushikesh.fitscore.model.ExtractedProfile;
import com.rushikesh.fitscore.model.FitScoreResult;
import com.rushikesh.fitscore.model.PageResponse;
import com.rushikesh.fitscore.model.ScoreBreakdown;
import com.rushikesh.fitscore.model.ScoreResponse;
import com.rushikesh.fitscore.repository.FitScoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;

@Service
public class FitScoreService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PdfTextExtractor pdfTextExtractor;
    private final ProfileExtractor profileExtractor;
    private final ScoreCalculator scoreCalculator;
    private final FitScoreRepository repository;

    public FitScoreService(PdfTextExtractor pdfTextExtractor,
                           ProfileExtractor profileExtractor,
                           ScoreCalculator scoreCalculator,
                           FitScoreRepository repository) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.profileExtractor = profileExtractor;
        this.scoreCalculator = scoreCalculator;
        this.repository = repository;
    }

    public ScoreResponse analyze(MultipartFile resumeFile, String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException("Job description must not be empty");
        }

        String resumeText = pdfTextExtractor.extract(resumeFile);

        ExtractedProfile resume = profileExtractor.fromResume(resumeText);
        ExtractedProfile jd = profileExtractor.fromJobDescription(jobDescription);

        ScoreBreakdown breakdown = scoreCalculator.calculate(resume, jd);

        FitScoreResult saved = repository.save(new FitScoreResult(
                resumeFile.getOriginalFilename(),
                breakdown.totalScore(),
                breakdown.skillScore(),
                breakdown.experienceScore(),
                breakdown.matchedSkills(),
                breakdown.missingSkills()));

        return ScoreResponse.from(saved);
    }

    public ScoreResponse getById(Long id) {
        return repository.findById(id)
                .map(ScoreResponse::from)
                .orElseThrow(() -> new NoSuchElementException("No result found with id " + id));
    }

    /** Newest first, page size capped so a client cannot ask for everything at once. */
    public PageResponse<ScoreResponse> list(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        var pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(repository.findAll(pageRequest).map(ScoreResponse::from));
    }
}
