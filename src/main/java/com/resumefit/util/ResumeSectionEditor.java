package com.resumefit.util;

import com.resumefit.dto.ResumeSectionDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ResumeSectionEditor {

    private static final List<String> SUMMARY_TITLES = List.of("SUMMARY", "PROFILE", "PROFESSIONAL SUMMARY");
    private static final List<String> SKILL_TITLES = List.of("SKILLS", "TECHNICAL SKILLS", "CORE SKILLS");
    private static final List<String> EXPERIENCE_TITLES = List.of("EXPERIENCE", "WORK EXPERIENCE", "PROFESSIONAL EXPERIENCE", "INTERNSHIPS", "INTERNSHIP EXPERIENCE");
    private static final List<String> PROJECT_TITLES = List.of("PROJECTS", "ACADEMIC PROJECTS", "SELECTED PROJECTS");
    private static final List<String> EDUCATION_TITLES = List.of("EDUCATION");

    private ResumeSectionEditor() {
    }

    public static String optimizePlainTextResume(
            String resumeText,
            String roleType,
            String candidateStage,
            List<String> userSkills,
            List<String> jdKeywords,
            List<String> missingKeywords
    ) {
        List<ResumeSectionDto> sections = ResumeSectionParser.parse(resumeText);
        List<String> displaySkills = distinctTerms(userSkills);
        List<String> displayJdKeywords = distinctTerms(jdKeywords);
        List<String> preferredOrder = preferredSectionOrder(candidateStage, roleType, displayJdKeywords);

        if (sections.isEmpty()) {
            return buildFromUnstructuredText(resumeText, displaySkills, displayJdKeywords);
        }

        sections = reorderSections(sections, preferredOrder);

        List<String> output = new ArrayList<>();
        boolean hasSummary = false;
        boolean hasSkills = false;

        for (ResumeSectionDto section : sections) {
            String title = section.getTitle();
            List<String> optimizedLines = optimizeSectionLines(title, section.getLines(), displaySkills, displayJdKeywords);

            if (optimizedLines.isEmpty()) {
                continue;
            }

            if (isSummaryTitle(title)) {
                hasSummary = true;
            }
            if (isSkillTitle(title)) {
                hasSkills = true;
            }

            output.add(title);
            output.addAll(optimizedLines);
            output.add("");
        }

        if (!hasSummary) {
            output.add(0, "");
            output.add(0, buildSummary(displaySkills, displayJdKeywords));
            output.add(0, "SUMMARY");
        }

        if (!hasSkills) {
            output.add("");
            output.add("SKILLS");
            output.add(buildSkillsLine(displaySkills, displayJdKeywords));
        }

        return String.join("\n", output).replaceAll("\\n{3,}", "\n\n").trim();
    }

    public static String buildRoleBasedResume(
            String resumeText,
            String roleType,
            String candidateStage,
            List<String> roleGuidelines,
            List<String> userSkills
    ) {
        List<ResumeSectionDto> sections = ResumeSectionParser.parse(resumeText);
        List<String> displaySkills = distinctTerms(userSkills);
        sections = reorderSections(sections, preferredSectionOrder(candidateStage, roleType, roleGuidelines));
        List<String> output = new ArrayList<>();
        boolean hasSummary = false;

        for (ResumeSectionDto section : sections) {
            String title = section.getTitle();
            List<String> lines = new ArrayList<>(section.getLines());

            if (isSummaryTitle(title)) {
                lines = List.of(buildRoleSummary(roleType, displaySkills, roleGuidelines));
                hasSummary = true;
            } else if (isSkillTitle(title)) {
                lines = List.of(buildSkillsLine(displaySkills, List.of()));
            } else if (isExperienceLike(title) || isProjectTitle(title)) {
                lines = reorderByRoleFocus(lines, displaySkills, roleGuidelines);
            }

            if (lines.isEmpty()) {
                continue;
            }

            output.add(title);
            output.addAll(lines);
            output.add("");
        }

        if (!hasSummary) {
            output.add(0, "");
            output.add(0, buildRoleSummary(roleType, displaySkills, roleGuidelines));
            output.add(0, "SUMMARY");
        }

        return String.join("\n", output).replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static String buildFromUnstructuredText(String resumeText, List<String> userSkills, List<String> jdKeywords) {
        List<String> output = new ArrayList<>();
        output.add("SUMMARY");
        output.add(buildSummary(userSkills, jdKeywords));
        output.add("");
        output.add("SKILLS");
        output.add(buildSkillsLine(userSkills, jdKeywords));
        output.add("");
        output.add("RESUME CONTENT");
        output.addAll(ResumeTextCleaner.clean(resumeText).lines().toList());
        return String.join("\n", output).trim();
    }

    private static List<String> optimizeSectionLines(
            String title,
            List<String> lines,
            List<String> userSkills,
            List<String> jdKeywords
    ) {
        if (isSummaryTitle(title)) {
            return List.of(buildSummary(userSkills, jdKeywords));
        }
        if (isSkillTitle(title)) {
            return List.of(buildSkillsLine(userSkills, mergeKeywords(lines, jdKeywords)));
        }
        if (isExperienceLike(title) || isProjectTitle(title)) {
            return reorderByRelevance(lines, userSkills, jdKeywords);
        }
        if (isEducationTitle(title)) {
            return prioritizeEducationLines(lines, jdKeywords);
        }
        return cleanLines(lines);
    }

    private static List<String> reorderByRelevance(List<String> lines, List<String> userSkills, List<String> jdKeywords) {
        return ensureBulletStyle(cleanLines(lines)).stream()
                .sorted(Comparator
                        .comparingInt((String line) -> linePriorityScore(line, userSkills, jdKeywords))
                        .reversed()
                        .thenComparing(Comparator.comparingInt(String::length).reversed()))
                .toList();
    }

    private static List<String> reorderByRoleFocus(List<String> lines, List<String> userSkills, List<String> roleGuidelines) {
        List<String> focusTerms = distinctTerms(roleGuidelines);
        return ensureBulletStyle(cleanLines(lines)).stream()
                .sorted(Comparator
                        .comparingInt((String line) -> linePriorityScore(line, userSkills, focusTerms))
                        .reversed()
                        .thenComparing(Comparator.comparingInt(String::length).reversed()))
                .toList();
    }

    private static List<String> prioritizeEducationLines(List<String> lines, List<String> jdKeywords) {
        return ensureBulletStyle(cleanLines(lines)).stream()
                .sorted(Comparator
                        .comparingInt((String line) -> linePriorityScore(line, List.of(), jdKeywords))
                        .reversed()
                        .thenComparing((String line) -> containsMetric(line) ? 0 : 1))
                .toList();
    }

    private static int linePriorityScore(String line, List<String> primaryTerms, List<String> secondaryTerms) {
        String normalizedLine = KeywordExtractor.normalize(line);
        int score = 0;

        for (String term : primaryTerms) {
            String normalizedTerm = KeywordExtractor.normalize(term);
            if (!normalizedTerm.isBlank() && normalizedLine.contains(normalizedTerm)) {
                score += 6;
            }
        }
        for (String term : secondaryTerms) {
            String normalizedTerm = KeywordExtractor.normalize(term);
            if (!normalizedTerm.isBlank() && normalizedLine.contains(normalizedTerm)) {
                score += 4;
            }
        }

        if (containsMetric(line)) {
            score += 3;
        }
        if (line.trim().startsWith("-")) {
            score += 2;
        }
        if (normalizedLine.contains("built")
                || normalizedLine.contains("developed")
                || normalizedLine.contains("implemented")
                || normalizedLine.contains("designed")
                || normalizedLine.contains("optimized")) {
            score += 2;
        }

        return score;
    }

    private static boolean containsMetric(String line) {
        return line.matches("(?i).*\\b\\d+(%|\\+|x|k|m|months?|years?)?\\b.*");
    }

    private static List<ResumeSectionDto> reorderSections(List<ResumeSectionDto> sections, List<String> preferredOrder) {
        Map<String, Integer> orderMap = new java.util.HashMap<>();
        for (int index = 0; index < preferredOrder.size(); index++) {
            orderMap.put(preferredOrder.get(index), index);
        }

        return sections.stream()
                .sorted(Comparator.comparingInt(section -> orderMap.getOrDefault(normalizeSectionKey(section.getTitle()), 100)))
                .toList();
    }

    private static List<String> preferredSectionOrder(String candidateStage, String roleType, List<String> keywords) {
        String stage = candidateStage == null ? "" : candidateStage.toLowerCase(Locale.ROOT);
        String role = roleType == null ? "" : roleType.toLowerCase(Locale.ROOT);
        String combined = String.join(" ", keywords).toLowerCase(Locale.ROOT) + " " + role;

        if (stage.contains("intern")) {
            return List.of("SUMMARY", "SKILLS", "PROJECTS", "EDUCATION", "EXPERIENCE", "CERTIFICATIONS", "ACHIEVEMENTS");
        }
        if (stage.contains("fresher") || combined.contains("entry level") || combined.contains("fresher")) {
            return List.of("SUMMARY", "SKILLS", "PROJECTS", "EDUCATION", "CERTIFICATIONS", "ACHIEVEMENTS", "EXPERIENCE");
        }
        if (stage.contains("experienced")) {
            return List.of("SUMMARY", "SKILLS", "EXPERIENCE", "PROJECTS", "EDUCATION", "CERTIFICATIONS", "ACHIEVEMENTS");
        }
        if (combined.contains("intern")) {
            return List.of("SUMMARY", "SKILLS", "PROJECTS", "EDUCATION", "EXPERIENCE", "CERTIFICATIONS", "ACHIEVEMENTS");
        }
        return List.of("SUMMARY", "SKILLS", "EXPERIENCE", "PROJECTS", "EDUCATION", "CERTIFICATIONS", "ACHIEVEMENTS");
    }

    private static String normalizeSectionKey(String title) {
        if (isSummaryTitle(title)) {
            return "SUMMARY";
        }
        if (isSkillTitle(title)) {
            return "SKILLS";
        }
        if (isProjectTitle(title)) {
            return "PROJECTS";
        }
        if (isEducationTitle(title)) {
            return "EDUCATION";
        }
        if (isExperienceLike(title)) {
            return "EXPERIENCE";
        }
        return title;
    }

    private static List<String> ensureBulletStyle(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> line.startsWith("-") || line.length() < 32 ? line : "- " + line)
                .toList();
    }

    private static String buildSummary(List<String> userSkills, List<String> jdKeywords) {
        String skillFocus = String.join(", ", userSkills.stream().limit(4).toList());
        String jdFocus = String.join(", ", jdKeywords.stream().limit(5).toList());

        if (skillFocus.isBlank() && jdFocus.isBlank()) {
            return "ATS-focused candidate with truthful project and skill alignment, clear communication, and a strong focus on role-relevant outcomes.";
        }

        if (skillFocus.isBlank()) {
            return "Candidate aligned to roles requiring " + jdFocus
                    + ", with emphasis on practical implementation, clean execution, and ATS-friendly presentation.";
        }

        if (jdFocus.isBlank()) {
            return "Candidate with hands-on work across " + skillFocus
                    + ", presented with truthful, ATS-friendly positioning and clear project relevance.";
        }

        return "Candidate with hands-on experience across " + skillFocus
                + ", aligned to roles requiring " + jdFocus
                + ". Focused on clean implementation, project relevance, and interview-defensible resume positioning.";
    }

    private static String buildRoleSummary(String roleType, List<String> userSkills, List<String> roleGuidelines) {
        String skillFocus = String.join(", ", userSkills.stream().limit(4).toList());
        String emphasis = String.join(", ", distinctTerms(roleGuidelines).stream().limit(4).toList());

        if (skillFocus.isBlank()) {
            return "Targeting " + roleType + " opportunities with truthful project evidence, strong fundamentals, and ATS-ready structure.";
        }

        return "Targeting " + roleType + " opportunities with hands-on work across "
                + skillFocus
                + (emphasis.isBlank() ? "." : ", with emphasis on " + emphasis + ".")
                + " Resume content stays aligned to real experience and project scope.";
    }

    private static String buildSkillsLine(List<String> userSkills, List<String> jdKeywords) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        userSkills.stream().filter(skill -> !skill.isBlank()).forEach(merged::add);
        jdKeywords.stream()
                .filter(keyword -> containsIgnoreCase(userSkills, keyword))
                .forEach(merged::add);
        return String.join(" | ", merged);
    }

    private static List<String> mergeKeywords(List<String> lines, List<String> jdKeywords) {
        Set<String> lineKeywords = cleanLines(lines).stream()
                .flatMap(line -> normalizedTerms(List.of(line)).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<String> merged = new LinkedHashSet<>(lineKeywords);
        merged.addAll(jdKeywords);
        return List.copyOf(merged);
    }

    private static List<String> cleanLines(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private static boolean isSummaryTitle(String title) {
        return SUMMARY_TITLES.contains(title);
    }

    private static boolean isSkillTitle(String title) {
        return SKILL_TITLES.contains(title);
    }

    private static boolean isExperienceLike(String title) {
        return EXPERIENCE_TITLES.contains(title);
    }

    private static boolean isProjectTitle(String title) {
        return PROJECT_TITLES.contains(title);
    }

    private static boolean isEducationTitle(String title) {
        return EDUCATION_TITLES.contains(title);
    }

    private static List<String> distinctTerms(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .flatMap(value -> List.of(value.split("[,|\\n]")).stream())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private static List<String> normalizedTerms(List<String> values) {
        return distinctTerms(values).stream()
                .map(KeywordExtractor::normalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private static boolean containsIgnoreCase(List<String> values, String target) {
        return values.stream().anyMatch(value -> value.equalsIgnoreCase(target));
    }
}
