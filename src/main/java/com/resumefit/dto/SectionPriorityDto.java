package com.resumefit.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SectionPriorityDto {
    String section;
    int priority;
    String reason;
}
