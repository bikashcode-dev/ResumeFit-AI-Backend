package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeVersionResponse {
    String roleType;
    String selectedCandidateStage;
    String detectedCandidateStage;
    String profileMismatchWarning;
    List<String> recommendedOrder;
    String generatedResume;
    boolean aiGenerated;
    List<String> roleGuidelines;
}
