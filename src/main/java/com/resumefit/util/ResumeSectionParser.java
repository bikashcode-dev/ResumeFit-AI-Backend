package com.resumefit.util;

import com.resumefit.dto.ResumeSectionDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ResumeSectionParser {

    private ResumeSectionParser() {
    }

    public static List<ResumeSectionDto> parse(String text) {
        List<ResumeSectionDto> sections = new ArrayList<>();
        List<String> currentLines = new ArrayList<>();
        String currentTitle = "General";

        for (String line : ResumeTextCleaner.clean(text).split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            if (isHeading(trimmed)) {
                if (!currentLines.isEmpty()) {
                    sections.add(ResumeSectionDto.builder()
                            .title(currentTitle)
                            .lines(List.copyOf(currentLines))
                            .build());
                    currentLines = new ArrayList<>();
                }
                currentTitle = normalizeHeading(trimmed);
            } else {
                currentLines.add(trimmed);
            }
        }

        if (!currentLines.isEmpty()) {
            sections.add(ResumeSectionDto.builder()
                    .title(currentTitle)
                    .lines(List.copyOf(currentLines))
                    .build());
        }

        return sections;
    }

    private static boolean isHeading(String line) {
        String lower = line.toLowerCase(Locale.ROOT).trim();
        return List.of(
                "summary", "profile", "professional summary", "skills", "technical skills", "core skills",
                "experience", "work experience", "professional experience", "projects", "education",
                "certifications", "achievements", "internships"
        ).contains(lower);
    }

    private static String normalizeHeading(String line) {
        return line.trim().toUpperCase(Locale.ROOT);
    }
}
