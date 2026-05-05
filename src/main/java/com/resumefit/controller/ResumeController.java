package com.resumefit.controller;

import com.resumefit.dto.ResumeTailorRequest;
import com.resumefit.dto.ResumeTailorResponse;
import com.resumefit.service.ResumeTailoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeTailoringService resumeTailoringService;

    public ResumeController(ResumeTailoringService resumeTailoringService) {
        this.resumeTailoringService = resumeTailoringService;
    }

    @PostMapping("/tailor")
    public ResponseEntity<ResumeTailorResponse> tailorResume(
            @Valid @RequestBody ResumeTailorRequest request
    ) {
        return ResponseEntity.ok(resumeTailoringService.tailor(request));
    }
}
