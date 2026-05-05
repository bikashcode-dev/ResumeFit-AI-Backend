package com.resumefit.service;

import com.resumefit.dto.KeywordDto;
import com.resumefit.dto.KeywordMatchDto;
import com.resumefit.dto.KeywordTierAnalysisDto;
import com.resumefit.dto.ResumeMatchRequest;
import com.resumefit.dto.ResumeMatchResponse;
import com.resumefit.dto.ScoreBreakdownDto;
import com.resumefit.dto.SectionPriorityDto;
import com.resumefit.dto.SkillGapAnalysisDto;
import com.resumefit.dto.TruthAnalysisDto;
import com.resumefit.util.KeywordExtractor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ResumeMatchingService {

    private final ResumeSectionPriorityService sectionPriorityService;

    public ResumeMatchingService(ResumeSectionPriorityService sectionPriorityService) {
        this.sectionPriorityService = sectionPriorityService;
    }

    public ResumeMatchResponse match(ResumeMatchRequest request) {
        List<KeywordDto> jdKeywords = KeywordExtractor.extract(request.getJobDescription());
        String normalizedResumeText = KeywordExtractor.normalize(request.getResumeText());
        String normalizedSkillText = KeywordExtractor.normalize(nullToEmpty(request.getSkills()));
        String searchableCandidateText = normalizedResumeText + " " + normalizedSkillText;

        List<KeywordMatchDto> matched = jdKeywords.stream()
                .filter(keyword -> containsKeyword(searchableCandidateText, keyword.getKeyword()))
                .map(this::toMatchDto)
                .toList();

        List<KeywordMatchDto> missing = jdKeywords.stream()
                .filter(keyword -> !containsKeyword(searchableCandidateText, keyword.getKeyword()))
                .map(this::toMatchDto)
                .toList();

        List<KeywordMatchDto> coveredByUserSkills = jdKeywords.stream()
                .filter(keyword -> !containsKeyword(normalizedResumeText, keyword.getKeyword()))
                .filter(keyword -> containsKeyword(normalizedSkillText, keyword.getKeyword()))
                .map(this::toMatchDto)
                .toList();

        List<KeywordMatchDto> criticalMissing = missing.stream()
                .filter(keyword -> List.of("tool", "skill").contains(keyword.getCategory()))
                .filter(keyword -> coveredByUserSkills.stream().noneMatch(covered -> covered.getKeyword().equals(keyword.getKeyword())))
                .toList();

        List<KeywordMatchDto> optionalMissing = missing.stream()
                .filter(keyword -> !List.of("tool", "skill").contains(keyword.getCategory()))
                .filter(keyword -> coveredByUserSkills.stream().noneMatch(covered -> covered.getKeyword().equals(keyword.getKeyword())))
                .toList();

        int totalWeight = jdKeywords.stream().mapToInt(this::weightOf).sum();
        int matchedWeight = matched.stream().mapToInt(KeywordMatchDto::getWeight).sum();
        int score = totalWeight == 0 ? 0 : Math.round((matchedWeight / (float) totalWeight) * 100);
        KeywordTierAnalysisDto keywordTiers = buildKeywordTiers(jdKeywords);
        TruthAnalysisDto truthAnalysis = buildTruthAnalysis(request, missing, coveredByUserSkills);
        String detectedCandidateStage = sectionPriorityService.getProfileAnalyzerService()
                .detectCandidateStage(request.getJobDescription(), "", request.getResumeText());
        String finalCandidateStage = sectionPriorityService.getProfileAnalyzerService()
                .resolveFinalCandidateStage(request.getCandidateStage(), detectedCandidateStage);
        String profileMismatchWarning = sectionPriorityService.getProfileAnalyzerService()
                .mismatchWarning(request.getCandidateStage(), detectedCandidateStage);
        List<SectionPriorityDto> sectionPriorities = sectionPriorityService.getProfileAnalyzerService()
                .recommendOrder(finalCandidateStage, "");

        return ResumeMatchResponse.builder()
                .matchScore(Math.min(score, 100))
                .matchedKeywords(matched)
                .missingKeywords(missing)
                .skillGapAnalysis(SkillGapAnalysisDto.builder()
                        .criticalMissing(criticalMissing)
                        .optionalMissing(optionalMissing)
                        .coveredByUserSkills(coveredByUserSkills)
                        .build())
                .scoreBreakdown(buildScoreBreakdown(jdKeywords, matched, request.getResumeText()))
                .keywordTiers(keywordTiers)
                .truthAnalysis(truthAnalysis)
                .selectedCandidateStage(finalCandidateStage)
                .detectedCandidateStage(detectedCandidateStage)
                .profileMismatchWarning(profileMismatchWarning)
                .sectionPriorities(sectionPriorities)
                .explanation(buildExplanation(totalWeight, matchedWeight, matched.size(), missing.size(), truthAnalysis, keywordTiers, sectionPriorities))
                .build();
    }

    private boolean containsKeyword(String normalizedResume, String keyword) {
        String normalizedKeyword = KeywordExtractor.normalize(keyword);
        if (normalizedKeyword.isBlank()) {
            return false;
        }

        String pattern = "(^|\\s)" + Pattern.quote(normalizedKeyword.toLowerCase(Locale.ROOT)) + "(\\s|$)";
        return Pattern.compile(pattern).matcher(normalizedResume).find();
    }

    private KeywordMatchDto toMatchDto(KeywordDto keyword) {
        return KeywordMatchDto.builder()
                .keyword(keyword.getKeyword())
                .category(keyword.getCategory())
                .weight(weightOf(keyword))
                .build();
    }

    private int weightOf(KeywordDto keyword) {
        return switch (keyword.getCategory()) {
            case "tool" -> 5;
            case "skill" -> 4;
            case "role" -> 3;
            default -> 1;
        };
    }

    private List<String> buildExplanation(
            int totalWeight,
            int matchedWeight,
            int matchedCount,
            int missingCount,
            TruthAnalysisDto truthAnalysis,
            KeywordTierAnalysisDto keywordTiers,
            List<SectionPriorityDto> sectionPriorities
    ) {
        List<String> lines = new ArrayList<>(List.of(
                "Skills and tools have higher weight than generic words because ATS and recruiters usually screen for role-fit capabilities first.",
                "Tool keywords weigh 5, skill keywords weigh 4, role keywords weigh 3, and general repeated JD terms weigh 1.",
                "Candidate-provided skills are included with resume text, but missing keywords should only be added to the resume if truthful.",
                "Score formula: matched keyword weight / total JD keyword weight * 100.",
                "Matched weight: " + matchedWeight + " out of " + totalWeight + ". Matched keywords: " + matchedCount + ", missing keywords: " + missingCount + "."
        ));
        lines.add("Truth score: " + truthAnalysis.getTruthScore() + "/100 based on supported skills, risky gaps, and section evidence.");
        if (!keywordTiers.getMustHave().isEmpty()) {
            lines.add("Must-have JD terms to prioritize first: " + String.join(", ", keywordTiers.getMustHave().stream().limit(5).toList()) + ".");
        }
        if (!sectionPriorities.isEmpty()) {
            lines.add("Recommended section order focus: " + String.join(" > ", sectionPriorities.stream().limit(5).map(SectionPriorityDto::getSection).toList()) + ".");
        }
        return lines;
    }

    private KeywordTierAnalysisDto buildKeywordTiers(List<KeywordDto> jdKeywords) {
        List<String> mustHave = jdKeywords.stream()
                .filter(keyword -> weightOf(keyword) >= 4 || keyword.getImportance() >= 85)
                .map(KeywordDto::getKeyword)
                .distinct()
                .limit(8)
                .toList();

        List<String> goodToHave = jdKeywords.stream()
                .filter(keyword -> !mustHave.contains(keyword.getKeyword()))
                .filter(keyword -> keyword.getImportance() >= 65)
                .map(KeywordDto::getKeyword)
                .distinct()
                .limit(8)
                .toList();

        List<String> optional = jdKeywords.stream()
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

    private TruthAnalysisDto buildTruthAnalysis(
            ResumeMatchRequest request,
            List<KeywordMatchDto> missingKeywords,
            List<KeywordMatchDto> coveredByUserSkills
    ) {
        String normalizedResume = KeywordExtractor.normalize(request.getResumeText());
        String normalizedSkills = KeywordExtractor.normalize(nullToEmpty(request.getSkills()));
        List<String> safeSignals = new ArrayList<>();
        List<String> riskySignals = new ArrayList<>();
        int truthScore = 88;

        if (normalizedResume.contains("project")) {
            safeSignals.add("Projects section present for concrete proof.");
        }
        if (normalizedResume.matches("(?s).*\\b\\d+(%|\\+|x|k|m)?\\b.*")) {
            safeSignals.add("Metrics or measurable proof detected.");
        }
        if (!coveredByUserSkills.isEmpty()) {
            safeSignals.add("Some JD terms are backed by your confirmed skills.");
        }

        long unsupportedCritical = missingKeywords.stream()
                .filter(keyword -> List.of("tool", "skill").contains(keyword.getCategory()))
                .filter(keyword -> !containsKeyword(normalizedSkills, keyword.getKeyword()))
                .count();

        if (unsupportedCritical > 0) {
            riskySignals.add("Critical JD tools/skills are still unsupported by resume or confirmed skills.");
            truthScore -= Math.min(unsupportedCritical * 7, 28);
        }
        if (!normalizedResume.contains("project") && !normalizedResume.contains("experience")) {
            riskySignals.add("Resume lacks clear project or experience proof sections.");
            truthScore -= 20;
        }
        if (normalizedResume.length() < 500) {
            riskySignals.add("Resume content is short, which can weaken ATS and recruiter confidence.");
            truthScore -= 8;
        }

        return TruthAnalysisDto.builder()
                .truthScore(Math.max(0, Math.min(100, truthScore)))
                .safeSignals(safeSignals)
                .riskySignals(riskySignals)
                .build();
    }

    private ScoreBreakdownDto buildScoreBreakdown(
            List<KeywordDto> jdKeywords,
            List<KeywordMatchDto> matchedKeywords,
            String resumeText
    ) {
        return ScoreBreakdownDto.builder()
                .skillsMatch(categoryScore(jdKeywords, matchedKeywords, "skill"))
                .toolsMatch(categoryScore(jdKeywords, matchedKeywords, "tool"))
                .roleMatch(categoryScore(jdKeywords, matchedKeywords, "role"))
                .atsStructure(calculateStructureScore(resumeText))
                .readability(calculateReadabilityScore(resumeText))
                .build();
    }

    private int categoryScore(List<KeywordDto> allKeywords, List<KeywordMatchDto> matchedKeywords, String category) {
        long total = allKeywords.stream().filter(keyword -> category.equals(keyword.getCategory())).count();
        long matched = matchedKeywords.stream().filter(keyword -> category.equals(keyword.getCategory())).count();
        if (total == 0) {
            return 100;
        }
        return (int) Math.round((matched / (double) total) * 100);
    }

    private int calculateStructureScore(String resumeText) {
        String lower = resumeText.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String heading : List.of("summary", "skills", "experience", "projects", "education")) {
            if (lower.contains(heading)) {
                score += 20;
            }
        }
        return Math.min(score, 100);
    }

    private int calculateReadabilityScore(String resumeText) {
        String lower = resumeText.toLowerCase(Locale.ROOT);
        int score = 40;
        if (lower.contains("- ")) {
            score += 20;
        }
        if (resumeText.matches("(?s).*\\b\\d+(%|\\+|x|k|m)?\\b.*")) {
            score += 20;
        }
        if (resumeText.length() > 500) {
            score += 20;
        }
        return Math.min(score, 100);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
