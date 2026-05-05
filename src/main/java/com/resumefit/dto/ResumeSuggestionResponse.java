package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeSuggestionResponse {
    int matchScore;
    List<SuggestionDto> suggestions;
}
