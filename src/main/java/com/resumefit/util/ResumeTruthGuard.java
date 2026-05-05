package com.resumefit.util;

import com.resumefit.dto.KeywordDto;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class ResumeTruthGuard {

    private ResumeTruthGuard() {
    }

    public static String keepOnlySafeOutput(
            String generatedResume,
            String sourceResume,
            String userSkills,
            String jobDescription
    ) {
        if (generatedResume == null || generatedResume.isBlank()) {
            return "";
        }

        String cleanedGenerated = ResumeTextCleaner.clean(generatedResume);
        String cleanedSource = ResumeTextCleaner.clean(sourceResume);

        if (cleanedGenerated.isBlank()) {
            return "";
        }

        if (!hasEnoughContent(cleanedGenerated, cleanedSource)) {
            return "";
        }

        if (!preservesEnoughHeadings(cleanedGenerated, cleanedSource)) {
            return "";
        }

        if (introducesTooManyUnexpectedKeywords(cleanedGenerated, cleanedSource, userSkills, jobDescription)) {
            return "";
        }

        return cleanedGenerated;
    }

    private static boolean hasEnoughContent(String generatedResume, String sourceResume) {
        if (sourceResume == null || sourceResume.isBlank()) {
            return generatedResume.length() >= 200;
        }

        return generatedResume.length() >= Math.max(200, (int) (sourceResume.length() * 0.55));
    }

    private static boolean preservesEnoughHeadings(String generatedResume, String sourceResume) {
        List<String> sourceHeadings = headingLines(sourceResume);
        if (sourceHeadings.size() < 2) {
            return true;
        }

        Set<String> generatedHeadings = headingLines(generatedResume)
                .stream()
                .collect(Collectors.toSet());

        long preserved = sourceHeadings.stream()
                .filter(generatedHeadings::contains)
                .count();

        long minimumRequired = Math.max(2, Math.round(sourceHeadings.size() * 0.5f));
        return preserved >= minimumRequired;
    }

    private static boolean introducesTooManyUnexpectedKeywords(
            String generatedResume,
            String sourceResume,
            String userSkills,
            String jobDescription
    ) {
        Set<String> allowedKeywords = extractImportantKeywords(sourceResume + "\n" + nullToEmpty(userSkills) + "\n" + nullToEmpty(jobDescription));
        Set<String> generatedKeywords = extractImportantKeywords(generatedResume);

        long unexpectedCount = generatedKeywords.stream()
                .filter(keyword -> !allowedKeywords.contains(keyword))
                .count();

        return unexpectedCount > 2;
    }

    private static Set<String> extractImportantKeywords(String text) {
        return KeywordExtractor.extract(text)
                .stream()
                .filter(keyword -> List.of("tool", "skill").contains(keyword.getCategory()))
                .map(KeywordDto::getKeyword)
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static List<String> headingLines(String text) {
        return ResumeTextCleaner.clean(text)
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(ResumeTruthGuard::looksLikeHeading)
                .map(line -> line.toLowerCase(Locale.ROOT))
                .toList();
    }

    private static boolean looksLikeHeading(String line) {
        return line.length() <= 40
                && (line.equals(line.toUpperCase(Locale.ROOT))
                || List.of(
                        "summary",
                        "profile",
                        "skills",
                        "technical skills",
                        "experience",
                        "work experience",
                        "professional experience",
                        "projects",
                        "education",
                        "certifications"
                ).contains(line.toLowerCase(Locale.ROOT)));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
