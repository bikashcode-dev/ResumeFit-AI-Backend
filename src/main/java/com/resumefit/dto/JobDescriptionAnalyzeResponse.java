package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JobDescriptionAnalyzeResponse {
    List<KeywordDto> keywords;
    List<String> topKeywords;
    KeywordTierAnalysisDto keywordTiers;
    List<String> requiredSkills;
    List<String> tools;
    List<String> roleSignals;
    List<String> responsibilities;
    List<String> softSkills;
    String experienceLevel;
    String roleCategory;
}
