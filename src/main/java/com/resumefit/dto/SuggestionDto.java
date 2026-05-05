package com.resumefit.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SuggestionDto {
    String type;
    String priority;
    String title;
    String action;
    String reason;
}
