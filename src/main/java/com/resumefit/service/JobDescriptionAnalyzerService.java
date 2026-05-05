package com.resumefit.service;

import com.resumefit.dto.JobDescriptionAnalyzeRequest;
import com.resumefit.dto.JobDescriptionAnalyzeResponse;
import com.resumefit.dto.KeywordDto;
import com.resumefit.dto.KeywordTierAnalysisDto;
import com.resumefit.util.KeywordExtractor;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JobDescriptionAnalyzerService {

    public JobDescriptionAnalyzeResponse analyze(JobDescriptionAnalyzeRequest request) {
        List<KeywordDto> keywords = KeywordExtractor.extract(request.getJobDescription());
        String normalized = request.getJobDescription().toLowerCase();

        return JobDescriptionAnalyzeResponse.builder()
                .keywords(keywords)
                .topKeywords(keywords.stream().limit(12).map(KeywordDto::getKeyword).toList())
                .keywordTiers(buildKeywordTiers(keywords))
                .requiredSkills(filterByCategory(keywords, "skill"))
                .tools(filterByCategory(keywords, "tool"))
                .roleSignals(filterByCategory(keywords, "role"))
                .responsibilities(extractResponsibilities(request.getJobDescription()))
                .softSkills(extractSoftSkills(normalized))
                .experienceLevel(inferExperienceLevel(normalized))
                .roleCategory(inferRoleCategory(normalized))
                .build();
    }

    private KeywordTierAnalysisDto buildKeywordTiers(List<KeywordDto> keywords) {
        List<String> mustHave = keywords.stream()
                .filter(keyword -> keyword.getImportance() >= 85 || List.of("tool", "skill").contains(keyword.getCategory()))
                .map(KeywordDto::getKeyword)
                .distinct()
                .limit(8)
                .toList();

        List<String> goodToHave = keywords.stream()
                .filter(keyword -> !mustHave.contains(keyword.getKeyword()))
                .filter(keyword -> keyword.getImportance() >= 65)
                .map(KeywordDto::getKeyword)
                .distinct()
                .limit(8)
                .toList();

        List<String> optional = keywords.stream()
                .filter(keyword -> !mustHave.contains(keyword.getKeyword()))
                .filter(keyword -> !goodToHave.contains(keyword.getKeyword()))
                .map(KeywordDto::getKeyword)
                .distinct()
                .limit(8)
                .toList();

        return KeywordTierAnalysisDto.builder()
                .mustHave(mustHave)
                .goodToHave(goodToHave)
                .optional(optional)
                .build();
    }

    private List<String> filterByCategory(List<KeywordDto> keywords, String category) {
        return keywords.stream()
                .filter(keyword -> category.equals(keyword.getCategory()))
                .map(KeywordDto::getKeyword)
                .distinct()
                .limit(15)
                .toList();
    }

    private List<String> extractResponsibilities(String text) {
        return List.of(text.split("[\\r\\n]+"))
                .stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> {
                    String lower = line.toLowerCase();
                    return lower.contains("develop")
                            || lower.contains("build")
                            || lower.contains("design")
                            || lower.contains("maintain")
                            || lower.contains("collaborat")
                            || lower.contains("implement")
                            || lower.contains("support");
                })
                .limit(6)
                .toList();
    }

    private List<String> extractSoftSkills(String normalizedText) {
        return List.of("communication", "collaboration", "leadership", "ownership", "problem solving", "teamwork")
                .stream()
                .filter(normalizedText::contains)
                .toList();
    }

    private String inferExperienceLevel(String normalizedText) {
        if (normalizedText.contains("fresher") || normalizedText.contains("entry level") || normalizedText.contains("entry-level")) {
            return "Entry Level";
        }
        if (normalizedText.contains("senior") || normalizedText.contains("lead") || normalizedText.contains("5+ years")) {
            return "Senior";
        }
        if (normalizedText.contains("junior") || normalizedText.contains("1+ years") || normalizedText.contains("2+ years")) {
            return "Junior to Mid";
        }
        return "Not clearly specified";
    }

    private String inferRoleCategory(String normalizedText) {
        if (normalizedText.contains("java")) {
            return "Java Developer";
        }
        if (normalizedText.contains("backend") || normalizedText.contains("spring") || normalizedText.contains("api")) {
            return "Backend Developer";
        }
        if (normalizedText.contains("frontend") || normalizedText.contains("react")) {
            return "Frontend Developer";
        }
        if (normalizedText.contains("full stack") || normalizedText.contains("full-stack")) {
            return "Full Stack Developer";
        }
        return "General Software Role";
    }
}
