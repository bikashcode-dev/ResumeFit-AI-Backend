package com.resumefit.dto;

import java.util.List;

public record ResumeTailorResponse(
        String tailoredResume,
        AtsAnalysis ats,
        List<String> recruiterNotes,
        ResumeMetadata metadata
) {
}
