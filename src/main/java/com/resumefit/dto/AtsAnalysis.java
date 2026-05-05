package com.resumefit.dto;

import java.util.List;

public record AtsAnalysis(
        int score,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<String> recommendations
) {
}
