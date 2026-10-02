package com.rushikesh.fitscore.service;

import com.rushikesh.fitscore.model.ExtractedProfile;
import com.rushikesh.fitscore.model.FitScoreResult;
import com.rushikesh.fitscore.model.ScoreResponse;
import com.rushikesh.fitscore.repository.FitScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** The LLM is mocked, so this tests our flow without any AI or database. */
@ExtendWith(MockitoExtension.class)
class FitScoreServiceTest {

    @Mock PdfTextExtractor pdfTextExtractor;
    @Mock ProfileExtractor profileExtractor;
    @Mock FitScoreRepository repository;

    FitScoreService service;

    private final MockMultipartFile file =
            new MockMultipartFile("resume", "cv.pdf", "application/pdf", "x".getBytes());

    @BeforeEach
    void setUp() {
        service = new FitScoreService(pdfTextExtractor, profileExtractor, new ScoreCalculator(), repository);
    }

    @Test
    void analyzeScoresAndSavesResult() {
        when(pdfTextExtractor.extract(file)).thenReturn("resume text");
        when(profileExtractor.fromResume("resume text"))
                .thenReturn(new ExtractedProfile(List.of("Java", "Spring Boot"), 4));
        when(profileExtractor.fromJobDescription("jd text"))
                .thenReturn(new ExtractedProfile(List.of("Java", "Kafka"), 4));
        when(repository.save(any(FitScoreResult.class))).thenAnswer(inv -> inv.getArgument(0));

        ScoreResponse response = service.analyze(file, "jd text");

        assertEquals(65.0, response.totalScore());   // 35 (1 of 2 skills) + 30 (experience met)
        assertEquals(List.of("Java"), response.matchedSkills());
        assertEquals(List.of("Kafka"), response.missingSkills());
        verify(repository).save(any(FitScoreResult.class));
    }

    @Test
    void blankJobDescriptionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.analyze(file, "   "));
        verifyNoInteractions(pdfTextExtractor, profileExtractor, repository);
    }
}
