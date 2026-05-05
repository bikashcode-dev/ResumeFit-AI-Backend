package com.resumefit.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoreBreakdownDto {
    int skillsMatch;
    int toolsMatch;
    int roleMatch;
    int atsStructure;
    int readability;
}
