package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeMatchResponse {
    int matchScore;
    List<KeywordMatchDto> matchedKeywords;
    List<KeywordMatchDto> missingKeywords;
    SkillGapAnalysisDto skillGapAnalysis;
    ScoreBreakdownDto scoreBreakdown;
    KeywordTierAnalysisDto keywordTiers;
    TruthAnalysisDto truthAnalysis;
    String selectedCandidateStage;
    String detectedCandidateStage;
    String profileMismatchWarning;
    List<SectionPriorityDto> sectionPriorities;
    List<String> explanation;
}
