package com.resumefit.controller;

import com.resumefit.dto.ResumeBuilderRequest;
import com.resumefit.dto.ResumeBuilderResponse;
import com.resumefit.dto.ResumeMatchRequest;
import com.resumefit.dto.ResumeMatchResponse;
import com.resumefit.dto.ResumeExportRequest;
import com.resumefit.dto.ResumeOptimizeRequest;
import com.resumefit.dto.ResumeOptimizeResponse;
import com.resumefit.dto.ResumePdfExportRequest;
import com.resumefit.dto.ResumeSectionAssistRequest;
import com.resumefit.dto.ResumeSectionAssistResponse;
import com.resumefit.dto.ResumeSuggestionResponse;
import com.resumefit.dto.ResumeVersionRequest;
import com.resumefit.dto.ResumeVersionResponse;
import com.resumefit.service.ResumeBuilderService;
import com.resumefit.service.ResumeMatchingService;
import com.resumefit.service.ResumeExportService;
import com.resumefit.service.ResumePdfExportService;
import com.resumefit.service.ResumeSectionAssistService;
import com.resumefit.service.ResumeOptimizationService;
import com.resumefit.service.ResumeSuggestionService;
import com.resumefit.service.ResumeVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume")
public class ResumeIntelligenceController {

    private final ResumeMatchingService matchingService;
    private final ResumeBuilderService builderService;
    private final ResumeExportService exportService;
    private final ResumePdfExportService pdfExportService;
    private final ResumeSuggestionService suggestionService;
    private final ResumeSectionAssistService sectionAssistService;
    private final ResumeOptimizationService optimizationService;
    private final ResumeVersionService versionService;

    public ResumeIntelligenceController(
            ResumeMatchingService matchingService,
            ResumeBuilderService builderService,
            ResumeExportService exportService,
            ResumePdfExportService pdfExportService,
            ResumeSuggestionService suggestionService,
            ResumeSectionAssistService sectionAssistService,
            ResumeOptimizationService optimizationService,
            ResumeVersionService versionService
    ) {
        this.matchingService = matchingService;
        this.builderService = builderService;
        this.exportService = exportService;
        this.pdfExportService = pdfExportService;
        this.suggestionService = suggestionService;
        this.sectionAssistService = sectionAssistService;
        this.optimizationService = optimizationService;
        this.versionService = versionService;
    }

    @PostMapping("/match")
    public ResponseEntity<ResumeMatchResponse> match(@Valid @RequestBody ResumeMatchRequest request) {
        return ResponseEntity.ok(matchingService.match(request));
    }

    @PostMapping("/builder/generate")
    public ResponseEntity<ResumeBuilderResponse> generateFromDetails(@Valid @RequestBody ResumeBuilderRequest request) {
        return ResponseEntity.ok(builderService.generate(request));
    }

    @PostMapping("/suggestions")
    public ResponseEntity<ResumeSuggestionResponse> suggestions(@Valid @RequestBody ResumeMatchRequest request) {
        return ResponseEntity.ok(suggestionService.suggest(request));
    }

    @PostMapping("/builder/assist-section")
    public ResponseEntity<ResumeSectionAssistResponse> assistSection(@Valid @RequestBody ResumeSectionAssistRequest request) {
        return ResponseEntity.ok(sectionAssistService.assist(request));
    }

    @PostMapping("/optimize")
    public ResponseEntity<ResumeOptimizeResponse> optimize(@Valid @RequestBody ResumeOptimizeRequest request) {
        return ResponseEntity.ok(optimizationService.optimize(request));
    }

    @PostMapping("/versions")
    public ResponseEntity<ResumeVersionResponse> generateVersion(@Valid @RequestBody ResumeVersionRequest request) {
        return ResponseEntity.ok(versionService.generate(request));
    }

    @PostMapping("/export/docx")
    public ResponseEntity<byte[]> exportDocx(@Valid @RequestBody ResumeExportRequest request) {
        String fileName = (request.getFileName() == null || request.getFileName().isBlank())
                ? "optimized-resume.docx"
                : request.getFileName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(exportService.exportDocx(request));
    }

    @PostMapping("/export/pdf/{style}")
    public ResponseEntity<byte[]> exportPdf(
            @org.springframework.web.bind.annotation.PathVariable String style,
            @Valid @RequestBody ResumePdfExportRequest request
    ) {
        String safeStyle = switch (style.toLowerCase()) {
            case "minimal" -> "minimal";
            case "template" -> "template";
            default -> "ats";
        };
        String fileName = (request.getFileName() == null || request.getFileName().isBlank())
                ? "optimized-resume-" + safeStyle + ".pdf"
                : request.getFileName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfExportService.export(
                        request.getResumeText(),
                        safeStyle,
                        request.getTemplateProfile(),
                        request.getDocumentTitle()
                ));
    }
}
