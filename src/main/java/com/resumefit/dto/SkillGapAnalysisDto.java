package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SkillGapAnalysisDto {
    List<KeywordMatchDto> criticalMissing;
    List<KeywordMatchDto> optionalMissing;
    List<KeywordMatchDto> coveredByUserSkills;
}
