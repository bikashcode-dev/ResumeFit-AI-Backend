package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeOptimizeResponse {
    String optimizedResume;
    boolean aiGenerated;
    String selectedCandidateStage;
    String detectedCandidateStage;
    String profileMismatchWarning;
    List<String> recommendedOrder;
    List<String> appliedRules;
}
