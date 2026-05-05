package com.resumefit.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KeywordDto {
    String keyword;
    String category;
    int frequency;
    int importance;
}
