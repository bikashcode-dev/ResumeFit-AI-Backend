package com.resumefit.service;

import com.resumefit.dto.ResumeBuilderRequest;
import com.resumefit.dto.ResumeBuilderResponse;
import com.resumefit.dto.ResumeCustomSectionDto;
import com.resumefit.dto.ResumeOptimizeRequest;
import com.resumefit.dto.ResumeOptimizeResponse;
import com.resumefit.dto.ResumeSectionDto;
import com.resumefit.dto.ResumeTemplateProfileDto;
import com.resumefit.dto.ResumeVersionRequest;
import com.resumefit.dto.ResumeVersionResponse;
import com.resumefit.util.ResumeSectionParser;
import com.resumefit.util.ResumeTextCleaner;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ResumeBuilderService {

    private final ResumeOptimizationService optimizationService;
    private final ResumeVersionService versionService;
    private final ResumeTemplateProfileService templateProfileService;

    public ResumeBuilderService(
            ResumeOptimizationService optimizationService,
            ResumeVersionService versionService,
            ResumeTemplateProfileService templateProfileService
    ) {
        this.optimizationService = optimizationService;
        this.versionService = versionService;
        this.templateProfileService = templateProfileService;
    }

    public ResumeBuilderResponse generate(ResumeBuilderRequest request) {
        String baseResume = buildBaseResume(request);
        String marketBrief = buildMarketBrief(request);

        ResumeOptimizeResponse optimized = optimizationService.optimize(new ResumeOptimizeRequest(
                baseResume,
                marketBrief,
                request.getSkills(),
                request.getRoleType(),
                request.getCandidateLevel()
        ));

        ResumeVersionResponse versioned = versionService.generate(new ResumeVersionRequest(
                optimized.getOptimizedResume(),
                request.getRoleType(),
                marketBrief,
                request.getSkills(),
                request.getCandidateLevel()
        ));

        String finalResume = ResumeTextCleaner.clean(versioned.getGeneratedResume());
        List<ResumeSectionDto> sections = ResumeSectionParser.parse(finalResume);
        ResumeTemplateProfileDto profile = templateProfileService.buildProfile(finalResume, sections, 1, "docx");

        return ResumeBuilderResponse.builder()
                .baseResume(baseResume)
                .generatedResume(finalResume)
                .aiGenerated(optimized.isAiGenerated() || versioned.isAiGenerated())
                .roleType(request.getRoleType())
                .candidateLevel(request.getCandidateLevel())
                .templateProfile(profile)
                .appliedRules(buildRules(optimized, versioned))
                .build();
    }

    private String buildBaseResume(ResumeBuilderRequest request) {
        List<String> lines = new ArrayList<>();
        lines.add(request.getFullName().trim());
        String contact = contactLine(request);
        if (!contact.isBlank()) {
            lines.add(contact);
        }
        lines.add("");

        for (SectionBlock block : orderedSections(request)) {
            lines.add(block.heading());
            lines.addAll(block.lines());
            lines.add("");
        }

        return ResumeTextCleaner.clean(String.join("\n", lines));
    }

    private List<SectionBlock> orderedSections(ResumeBuilderRequest request) {
        Map<String, SectionBlock> blocks = new LinkedHashMap<>();

        if (shouldIncludeSummary(request)) {
            blocks.put("SUMMARY", new SectionBlock("SUMMARY", List.of(summaryText(request))));
        }
        if (shouldIncludeSkills(request)) {
            blocks.put("SKILLS", new SectionBlock("SKILLS", List.of(request.getSkills().trim())));
        }
        if (shouldIncludeProjects(request)) {
            blocks.put("PROJECTS", new SectionBlock(
                    projectHeading(request),
                    bulletLines(defaultIfBlank(request.getProjectDetails(), projectFallback(request)))
            ));
        }
        if (shouldIncludeEducation(request)) {
            blocks.put("EDUCATION", new SectionBlock(
                    "EDUCATION",
                    bulletLines(defaultIfBlank(request.getEducationDetails(), educationFallback(request)))
            ));
        }
        if (shouldIncludeExperience(request)) {
            blocks.put("EXPERIENCE", new SectionBlock(
                    experienceHeading(request),
                    bulletLines(request.getExperienceDetails())
            ));
        }
        if (shouldIncludeCertifications(request)) {
            blocks.put("CERTIFICATIONS", new SectionBlock(
                    "CERTIFICATIONS",
                    bulletLines(request.getCertifications())
            ));
        }
        if (shouldIncludeAchievements(request)) {
            blocks.put("ACHIEVEMENTS", new SectionBlock(
                    "ACHIEVEMENTS",
                    bulletLines(request.getAchievements())
            ));
        }
        if (request.getCustomSections() != null) {
            for (ResumeCustomSectionDto customSection : request.getCustomSections()) {
                if (customSection == null
                        || !isEnabled(customSection.getEnabled())
                        || !hasText(customSection.getTitle())
                        || !hasText(customSection.getContent())) {
                    continue;
                }
                String key = normalizeSectionKey(customSection.getSectionKey());
                if (key.isBlank()) {
                    key = normalizeSectionKey(customSection.getTitle());
                }
                blocks.put(key, new SectionBlock(
                        customSection.getTitle().trim().toUpperCase(),
                        bulletLines(customSection.getContent())
                ));
            }
        }

        List<String> requestedOrder = request.getSectionOrder() == null || request.getSectionOrder().isEmpty()
                ? List.of("SUMMARY", "SKILLS", "PROJECTS", "EDUCATION", "EXPERIENCE", "CERTIFICATIONS", "ACHIEVEMENTS")
                : request.getSectionOrder();

        List<SectionBlock> ordered = new ArrayList<>();
        for (String sectionKey : requestedOrder) {
            SectionBlock block = blocks.remove(normalizeSectionKey(sectionKey));
            if (block != null) {
                ordered.add(block);
            }
        }
        ordered.addAll(blocks.values());
        return ordered;
    }

    private String contactLine(ResumeBuilderRequest request) {
        List<String> items = new ArrayList<>();
        addIfPresent(items, request.getCurrentLocation());
        addIfPresent(items, request.getPhone());
        addIfPresent(items, request.getEmail());
        addIfPresent(items, request.getLinkedinUrl());
        addIfPresent(items, request.getGithubUrl());
        addIfPresent(items, request.getPortfolioUrl());
        return String.join(" | ", items);
    }

    private String summaryText(ResumeBuilderRequest request) {
        if (hasText(request.getSummary())) {
            return request.getSummary().trim();
        }
        String targetRole = request.getRoleType().trim();
        String skills = request.getSkills().trim();
        String candidateLevel = normalizedLevel(request.getCandidateLevel());
        if ("experienced".equals(candidateLevel)) {
            return "Results-focused " + targetRole.toLowerCase() + " professional with hands-on experience in "
                    + skills + ". Strong focus on maintainable delivery, collaboration, and ATS-friendly presentation.";
        }
        if ("intern".equals(candidateLevel)) {
            return "Motivated " + targetRole.toLowerCase()
                    + " intern candidate with strong academic and project-based exposure in " + skills
                    + ". Focused on learning quickly, writing reliable code, and contributing truthfully in team environments.";
        }
        return "Fresher candidate targeting " + targetRole.toLowerCase() + " roles with project-based experience in "
                + skills + ". Focused on strong fundamentals, practical builds, and truthful ATS-friendly positioning.";
    }

    private void addBullets(List<String> lines, String block) {
        for (String rawLine : block.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                continue;
            }
            lines.add(line.startsWith("-") ? line : "- " + line);
        }
    }

    private List<String> bulletLines(String block) {
        List<String> lines = new ArrayList<>();
        addBullets(lines, block);
        return lines;
    }

    private List<String> buildRules(ResumeOptimizeResponse optimized, ResumeVersionResponse versioned) {
        List<String> rules = new ArrayList<>(optimized.getAppliedRules());
        rules.add("Generated from user profile details, projects, education, and links.");
        rules.add("Candidate level aware builder flow used for " + versioned.getRoleType() + ".");
        rules.add("Role-based version alignment applied for " + versioned.getRoleType() + ".");
        return rules;
    }

    private String buildMarketBrief(ResumeBuilderRequest request) {
        String role = request.getRoleType().trim();
        String level = normalizedLevel(request.getCandidateLevel());
        List<String> lines = new ArrayList<>();
        lines.add("Target role: " + role);
        lines.add("Candidate level: " + request.getCandidateLevel());
        lines.add("Confirmed skills: " + request.getSkills().trim());
        lines.add("Resume must stay truthful and ATS-friendly.");
        lines.add("Prioritize only sections supported by the candidate details.");

        if ("experienced".equals(level)) {
            lines.add("Prioritize measurable work experience, production impact, delivery ownership, and maintainable engineering practices.");
        } else if ("intern".equals(level)) {
            lines.add("Prioritize academic projects, internship or training exposure, Git, debugging, teamwork, learning mindset, and practical fundamentals.");
        } else {
            lines.add("Prioritize education, academic or personal projects, certifications, GitHub, coursework, strong fundamentals, and interview-defensible claims.");
        }

        if (role.toLowerCase().contains("backend")) {
            lines.add("Highlight APIs, databases, Java or backend frameworks, debugging, testing, and service-side ownership.");
        } else if (role.toLowerCase().contains("full")) {
            lines.add("Highlight end-to-end delivery, frontend plus backend integration, APIs, UI, authentication, and database work.");
        } else if (role.toLowerCase().contains("frontend")) {
            lines.add("Highlight React, JavaScript, HTML, CSS, responsiveness, accessibility, and user-facing quality.");
        } else if (role.toLowerCase().contains("intern")) {
            lines.add("Use intern-friendly tone with strong fundamentals, teamwork, documentation, testing, and coding standards.");
        } else {
            lines.add("Adjust section ordering and wording to the target role while keeping all claims grounded.");
        }

        return String.join("\n", lines);
    }

    private boolean shouldIncludeExperience(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeExperience()) && hasText(request.getExperienceDetails());
    }

    private boolean shouldIncludeProjects(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeProjects())
                && (hasText(request.getProjectDetails()) || !"experienced".equals(normalizedLevel(request.getCandidateLevel())));
    }

    private boolean shouldIncludeEducation(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeEducation())
                && (hasText(request.getEducationDetails()) || !"experienced".equals(normalizedLevel(request.getCandidateLevel())));
    }

    private boolean shouldIncludeSummary(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeSummary());
    }

    private boolean shouldIncludeSkills(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeSkills()) && hasText(request.getSkills());
    }

    private boolean shouldIncludeCertifications(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeCertifications()) && hasText(request.getCertifications());
    }

    private boolean shouldIncludeAchievements(ResumeBuilderRequest request) {
        return isEnabled(request.getIncludeAchievements()) && hasText(request.getAchievements());
    }

    private String experienceHeading(ResumeBuilderRequest request) {
        String candidateLevel = normalizedLevel(request.getCandidateLevel());
        if ("intern".equals(candidateLevel)) {
            return "INTERNSHIP EXPERIENCE";
        }
        return "EXPERIENCE";
    }

    private String projectHeading(ResumeBuilderRequest request) {
        String candidateLevel = normalizedLevel(request.getCandidateLevel());
        if ("experienced".equals(candidateLevel)) {
            return "SELECTED PROJECTS";
        }
        return "ACADEMIC PROJECTS";
    }

    private String projectFallback(ResumeBuilderRequest request) {
        String candidateLevel = normalizedLevel(request.getCandidateLevel());
        if ("experienced".equals(candidateLevel)) {
            return "Add 2-4 strong projects that support the target role, tools used, and measurable outcomes.";
        }
        return "Add 2-4 academic or personal projects with role-relevant tools, responsibilities, and measurable outcomes if available.";
    }

    private String educationFallback(ResumeBuilderRequest request) {
        String candidateLevel = normalizedLevel(request.getCandidateLevel());
        if ("experienced".equals(candidateLevel)) {
            return "Add degree, institution, graduation year, and relevant coursework only if it supports the target role.";
        }
        return "Add degree, institution, graduation year, coursework, CGPA if strong, and role-relevant academic highlights.";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizedLevel(String candidateLevel) {
        if (!hasText(candidateLevel)) {
            return "fresher";
        }
        return candidateLevel.trim().toLowerCase();
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private void addIfPresent(List<String> items, String value) {
        if (hasText(value)) {
            items.add(value.trim());
        }
    }

    private boolean isEnabled(Boolean value) {
        return value == null || value;
    }

    private String normalizeSectionKey(String sectionKey) {
        return sectionKey == null ? "" : sectionKey.trim().toUpperCase();
    }

    private record SectionBlock(String heading, List<String> lines) {
    }
}
