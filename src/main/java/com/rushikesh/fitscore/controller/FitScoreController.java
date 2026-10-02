package com.rushikesh.fitscore.controller;

import com.rushikesh.fitscore.model.PageResponse;
import com.rushikesh.fitscore.model.ScoreResponse;
import com.rushikesh.fitscore.service.FitScoreService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fitscore")
public class FitScoreController {

    private final FitScoreService fitScoreService;

    public FitScoreController(FitScoreService fitScoreService) {
        this.fitScoreService = fitScoreService;
    }

    @Operation(summary = "Upload a resume (PDF) and a job description, get a fit score")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ScoreResponse analyze(@RequestParam("resume") MultipartFile resume,
                                 @RequestParam("jobDescription") String jobDescription) {
        return fitScoreService.analyze(resume, jobDescription);
    }

    @Operation(summary = "Scan history, newest first (paginated)")
    @GetMapping
    public PageResponse<ScoreResponse> history(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return fitScoreService.list(page, size);
    }

    @Operation(summary = "Fetch one previous result")
    @GetMapping("/{id}")
    public ScoreResponse getById(@PathVariable Long id) {
        return fitScoreService.getById(id);
    }
}
