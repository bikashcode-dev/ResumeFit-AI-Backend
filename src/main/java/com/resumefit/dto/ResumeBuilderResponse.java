package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeBuilderResponse {
    String baseResume;
    String generatedResume;
    boolean aiGenerated;
    String roleType;
    String candidateLevel;
    ResumeTemplateProfileDto templateProfile;
    List<String> appliedRules;
}
