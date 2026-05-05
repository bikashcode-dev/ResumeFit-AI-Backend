package com.resumefit.controller;

import com.resumefit.dto.JobDescriptionAnalyzeRequest;
import com.resumefit.dto.JobDescriptionAnalyzeResponse;
import com.resumefit.service.JobDescriptionAnalyzerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-descriptions")
public class JobDescriptionController {

    private final JobDescriptionAnalyzerService analyzerService;

    public JobDescriptionController(JobDescriptionAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<JobDescriptionAnalyzeResponse> analyze(
            @Valid @RequestBody JobDescriptionAnalyzeRequest request
    ) {
        return ResponseEntity.ok(analyzerService.analyze(request));
    }
}
