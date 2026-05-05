package com.resumefit.service;

import com.resumefit.dto.ResumeSectionAssistRequest;
import com.resumefit.dto.ResumeSectionAssistResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResumeSectionAssistService {

    private final AiPromptBuilder promptBuilder;
    private final ResumeAiService resumeAiService;

    public ResumeSectionAssistService(AiPromptBuilder promptBuilder, ResumeAiService resumeAiService) {
        this.promptBuilder = promptBuilder;
        this.resumeAiService = resumeAiService;
    }

    public ResumeSectionAssistResponse assist(ResumeSectionAssistRequest request) {
        String prompt = promptBuilder.buildSectionAssistPrompt(
                request.getSectionType(),
                request.getCurrentContent(),
                request.getRoleType(),
                request.getCandidateLevel(),
                request.getSkills()
        );

        return resumeAiService.complete(prompt)
                .map(this::cleanResult)
                .filter(result -> !result.isBlank())
                .map(result -> ResumeSectionAssistResponse.builder()
                        .sectionType(request.getSectionType())
                        .improvedContent(result)
                        .aiGenerated(true)
                        .appliedRules(buildRules(true, request.getSectionType()))
                        .build())
                .orElseGet(() -> ResumeSectionAssistResponse.builder()
                        .sectionType(request.getSectionType())
                        .improvedContent(buildFallback(request))
                        .aiGenerated(false)
                        .appliedRules(buildRules(false, request.getSectionType()))
                        .build());
    }

    private String buildFallback(ResumeSectionAssistRequest request) {
        String sectionType = normalize(request.getSectionType());
        String content = cleanResult(request.getCurrentContent());
        List<String> lines = content.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        if ("summary".equals(sectionType)) {
            String level = request.getCandidateLevel().trim();
            String role = request.getRoleType().trim();
            String skills = request.getSkills() == null ? "" : request.getSkills().trim();
            if (!content.isBlank()) {
                return content;
            }
            return "%s candidate targeting %s roles with practical exposure in %s. Focused on truthful ATS-friendly presentation, strong fundamentals, and role-relevant delivery."
                    .formatted(level, role, skills.isBlank() ? "real candidate-confirmed skills" : skills);
        }

        List<String> improved = new ArrayList<>();
        for (String line : lines) {
            String normalizedLine = line.startsWith("-") ? line.substring(1).trim() : line;
            if (normalizedLine.isBlank()) {
                continue;
            }
            if (!normalizedLine.matches(".*\\b(using|with|via|through)\\b.*")) {
                normalizedLine = normalizedLine + " using role-relevant tools and clear ownership";
            }
            if (!normalizedLine.matches(".*\\b(improved|built|developed|implemented|designed|created|delivered|debugged|tested|optimized)\\b.*")) {
                normalizedLine = "Built and delivered " + normalizedLine;
            }
            improved.add("- " + normalizedLine);
        }

        if (improved.isEmpty()) {
            return content;
        }
        return String.join("\n", improved);
    }

    private List<String> buildRules(boolean aiGenerated, String sectionType) {
        return List.of(
                aiGenerated
                        ? "AI improved the " + sectionType + " section with strict truth-safe prompting."
                        : "Fallback rules improved the " + sectionType + " section because AI was unavailable.",
                "Do not invent companies, dates, skills, metrics, or achievements.",
                "Keep the wording ATS-friendly and recruiter-readable.",
                "Prefer action-led, role-relevant language over generic filler."
        );
    }

    private String cleanResult(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("```text", "")
                .replace("```", "")
                .trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
