package com.resumefit.util;

import com.resumefit.dto.KeywordDto;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class KeywordExtractor {

    private static final int MAX_KEYWORDS = 40;
    private static final Pattern WORD_PATTERN = Pattern.compile("\\s+");
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "also", "and", "are", "based", "can", "candidate", "company",
            "for", "from", "has", "have", "into", "job", "must", "our", "role", "should",
            "that", "the", "their", "this", "with", "will", "work", "you", "your"
    );
    private static final Set<String> ROLE_WORDS = Set.of(
            "frontend", "backend", "fullstack", "full-stack", "developer", "engineer",
            "analyst", "designer", "manager", "intern", "architect", "tester", "qa"
    );
    private static final Set<String> TOOL_WORDS = Set.of(
            "react", "angular", "vue", "node", "express", "spring", "spring boot", "spring-boot",
            "java", "javascript", "typescript", "python", "sql", "mysql", "postgresql", "mongodb",
            "redis", "docker", "kubernetes", "aws", "azure", "gcp", "git", "github", "jira",
            "linux", "html", "css", "tailwind", "bootstrap", "rest", "graphql"
    );
    private static final Set<String> SKILL_WORDS = Set.of(
            "api", "apis", "testing", "debugging", "deployment", "documentation", "communication",
            "leadership", "collaboration", "performance", "security", "microservices", "responsive",
            "authentication", "authorization", "integration", "optimization", "analytics"
    );
    private static final List<String> IMPORTANT_PHRASES = List.of(
            "spring boot", "rest api", "react js", "node js", "full stack", "machine learning",
            "data analysis", "problem solving", "version control", "unit testing",
            "responsive design", "database design", "cloud deployment"
    );

    private KeywordExtractor() {
    }

    public static List<KeywordDto> extract(String text) {
        String normalizedText = normalize(text);
        Map<String, KeywordStats> stats = new LinkedHashMap<>();

        addImportantPhrases(normalizedText, stats);
        addSingleWords(normalizedText, stats);

        return stats.values()
                .stream()
                .sorted(Comparator.comparingInt(KeywordStats::importance).reversed()
                        .thenComparing(KeywordStats::keyword))
                .limit(MAX_KEYWORDS)
                .map(KeywordStats::toDto)
                .toList();
    }

    private static void addImportantPhrases(String text, Map<String, KeywordStats> stats) {
        for (String phrase : IMPORTANT_PHRASES) {
            int frequency = countOccurrences(text, phrase);

            if (frequency > 0) {
                stats.put(phrase, new KeywordStats(phrase, "skill", frequency, 90 + frequency));
            }
        }
    }

    private static void addSingleWords(String text, Map<String, KeywordStats> stats) {
        Map<String, Long> frequencies = WORD_PATTERN.splitAsStream(text)
                .map(String::trim)
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.groupingBy(word -> word, LinkedHashMap::new, Collectors.counting()));

        for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
            String word = entry.getKey();
            int frequency = entry.getValue().intValue();
            String category = categorize(word);
            int importance = calculateImportance(category, frequency);

            if (!"general".equals(category) || frequency > 1) {
                stats.putIfAbsent(word, new KeywordStats(word, category, frequency, importance));
            }
        }
    }

    private static int calculateImportance(String category, int frequency) {
        int categoryWeight = switch (category) {
            case "tool" -> 80;
            case "skill" -> 70;
            case "role" -> 60;
            default -> 35;
        };

        return categoryWeight + Math.min(frequency * 5, 20);
    }

    private static String categorize(String word) {
        if (TOOL_WORDS.contains(word)) {
            return "tool";
        }
        if (SKILL_WORDS.contains(word)) {
            return "skill";
        }
        if (ROLE_WORDS.contains(word)) {
            return "role";
        }
        return "general";
    }

    private static int countOccurrences(String text, String phrase) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(phrase, index)) >= 0) {
            count++;
            index += phrase.length();
        }

        return count;
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record KeywordStats(String keyword, String category, int frequency, int importance) {

        private KeywordDto toDto() {
            return KeywordDto.builder()
                    .keyword(keyword)
                    .category(category)
                    .frequency(frequency)
                    .importance(importance)
                    .build();
        }
    }
}
