package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TruthAnalysisDto {
    int truthScore;
    List<String> safeSignals;
    List<String> riskySignals;
}
