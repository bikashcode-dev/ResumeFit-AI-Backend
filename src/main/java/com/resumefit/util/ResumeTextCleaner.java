package com.resumefit.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class ResumeTextCleaner {

    private ResumeTextCleaner() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\f', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ");

        String lineCleaned = Arrays.stream(normalized.split("\\n"))
                .map(String::trim)
                .filter(line -> !isNoiseLine(line))
                .collect(Collectors.joining("\n"));

        return lineCleaned
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private static boolean isNoiseLine(String line) {
        if (line.isBlank()) {
            return false;
        }

        String lowerLine = line.toLowerCase();
        return lowerLine.matches("page\\s+\\d+(\\s+of\\s+\\d+)?")
                || lowerLine.matches("\\d+\\s*/\\s*\\d+")
                || lowerLine.matches("-{3,}");
    }
}
