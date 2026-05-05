package com.resumefit.controller;

import com.resumefit.dto.ResumeParseResponse;
import com.resumefit.service.ResumeParsingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeParsingController {

    private final ResumeParsingService resumeParsingService;

    public ResumeParsingController(ResumeParsingService resumeParsingService) {
        this.resumeParsingService = resumeParsingService;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeParseResponse> parseResume(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(resumeParsingService.parseResume(file));
    }
}
