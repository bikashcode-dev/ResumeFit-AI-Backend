package com.resumefit.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KeywordMatchDto {
    String keyword;
    String category;
    int weight;
}
