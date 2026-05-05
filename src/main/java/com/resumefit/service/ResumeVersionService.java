package com.resumefit.service;

import com.resumefit.dto.ResumeVersionRequest;
import com.resumefit.dto.ResumeVersionResponse;
import com.resumefit.util.ResumeSectionEditor;
import com.resumefit.util.ResumeTruthGuard;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ResumeVersionService {

    private final AiPromptBuilder promptBuilder;
    private final ResumeAiService aiClient;
    private final ResumeProfileAnalyzerService profileAnalyzerService;

    public ResumeVersionService(
            AiPromptBuilder promptBuilder,
            ResumeAiService aiClient,
            ResumeProfileAnalyzerService profileAnalyzerService
    ) {
        this.promptBuilder = promptBuilder;
        this.aiClient = aiClient;
        this.profileAnalyzerService = profileAnalyzerService;
    }

    public ResumeVersionResponse generate(ResumeVersionRequest request) {
        String normalizedRole = normalizeRole(request.getRoleType());
        List<String> guidelines = roleGuidelines(normalizedRole);
        String detectedCandidateStage = profileAnalyzerService.detectCandidateStage(
                request.getJobDescription(),
                normalizedRole,
                request.getResumeText()
        );
        String finalCandidateStage = profileAnalyzerService.resolveFinalCandidateStage(request.getCandidateStage(), detectedCandidateStage);
        String profileMismatchWarning = profileAnalyzerService.mismatchWarning(request.getCandidateStage(), detectedCandidateStage);
        List<String> recommendedOrder = profileAnalyzerService.recommendedOrderNames(finalCandidateStage, normalizedRole);
        String prompt = promptBuilder.buildVersionPrompt(
                request.getResumeText(),
                request.getJobDescription(),
                request.getSkills(),
                normalizedRole,
                finalCandidateStage,
                guidelines,
                recommendedOrder
        );

        return aiClient.complete(prompt)
                .map(generatedResume -> ResumeTruthGuard.keepOnlySafeOutput(
                        generatedResume,
                        request.getResumeText(),
                        request.getSkills(),
                        request.getJobDescription()
                ))
                .filter(safeResume -> !safeResume.isBlank())
                .map(generatedResume -> ResumeVersionResponse.builder()
                        .roleType(normalizedRole)
                        .selectedCandidateStage(finalCandidateStage)
                        .detectedCandidateStage(detectedCandidateStage)
                        .profileMismatchWarning(profileMismatchWarning)
                        .recommendedOrder(recommendedOrder)
                        .generatedResume(generatedResume)
                        .aiGenerated(true)
                        .roleGuidelines(guidelines)
                        .build())
                .orElseGet(() -> ResumeVersionResponse.builder()
                        .roleType(normalizedRole)
                        .selectedCandidateStage(finalCandidateStage)
                        .detectedCandidateStage(detectedCandidateStage)
                        .profileMismatchWarning(profileMismatchWarning)
                        .recommendedOrder(recommendedOrder)
                        .generatedResume(buildFallbackVersion(request.getResumeText(), request.getSkills(), normalizedRole, finalCandidateStage, guidelines))
                        .aiGenerated(false)
                        .roleGuidelines(guidelines)
                        .build());
    }

    private String normalizeRole(String roleType) {
        String role = roleType.toLowerCase(Locale.ROOT).trim();

        if (role.contains("java") && role.contains("backend")) {
            return "Java Backend Developer";
        }
        if (role.contains("frontend")) {
            return "Frontend Developer";
        }
        if (role.contains("full")) {
            return "Full Stack Developer";
        }
        if (role.contains("intern")) {
            return "Software Development Intern";
        }
        if (role.contains("java")) {
            return "Java Developer";
        }
        if (role.contains("backend")) {
            return "Backend Developer";
        }
        if (role.contains("fresh")) {
            return "Fresher";
        }

        return roleType.trim();
    }

    private List<String> roleGuidelines(String roleType) {
        return switch (roleType) {
            case "Java Backend Developer" -> List.of(
                    "Highlight Java, Spring Boot, REST APIs, SQL, debugging, and backend service ownership.",
                    "Use backend-focused engineering tone with API, database, and maintainability details.",
                    "Prioritize server-side project bullets before general programming content."
            );
            case "Java Developer" -> List.of(
                    "Highlight Java, Spring Boot, OOP, REST APIs, SQL, testing, and debugging.",
                    "Use confident engineering tone with backend implementation details.",
                    "Prioritize projects that show Java ecosystem skills."
            );
            case "Backend Developer" -> List.of(
                    "Highlight APIs, databases, authentication, performance, reliability, and deployment.",
                    "Keep bullets focused on systems, services, data flow, and maintainability.",
                    "Surface backend tools before frontend-only tools."
            );
            case "Frontend Developer" -> List.of(
                    "Highlight React, JavaScript, HTML, CSS, responsiveness, and user-facing impact.",
                    "Keep bullets focused on UI quality, accessibility, performance, and collaboration.",
                    "Prioritize frontend tools and product-facing project results."
            );
            case "Full Stack Developer" -> List.of(
                    "Balance frontend and backend evidence across projects, APIs, UI, and database work.",
                    "Show end-to-end delivery, integration, debugging, and maintainable architecture.",
                    "Keep tools ordered to reflect both client-side and server-side strength."
            );
            case "Software Development Intern" -> List.of(
                    "Use intern-friendly but confident tone focused on learning, contribution, and fundamentals.",
                    "Prioritize academic projects, Git, debugging, testing, teamwork, and documentation.",
                    "Avoid inflated ownership claims while still showing initiative and problem-solving."
            );
            case "Fresher" -> List.of(
                    "Use learning-focused but confident tone.",
                    "Prioritize education, projects, internships, certifications, and transferable skills.",
                    "Avoid overclaiming senior ownership or production-scale impact."
            );
            default -> List.of(
                    "Adjust summary, skills ordering, and bullets for the requested role.",
                    "Keep all claims consistent with the source resume.",
                    "Use ATS-friendly headings and plain text."
            );
        };
    }

    private String buildFallbackVersion(String resumeText, String skills, String roleType, String candidateStage, List<String> guidelines) {
        return ResumeSectionEditor.buildRoleBasedResume(
                resumeText,
                roleType,
                candidateStage,
                guidelines,
                splitSkills(skills)
        );
    }

    private List<String> splitSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return List.of();
        }

        return List.of(skills.split("[,|]"))
                .stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }
}
