package com.resumefit.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeCustomSectionDto {

    @Size(max = 80, message = "Section key must be under 80 characters.")
    private String sectionKey;

    @Size(max = 120, message = "Section title must be under 120 characters.")
    private String title;

    @Size(max = 5000, message = "Section content must be under 5000 characters.")
    private String content;

    private Boolean enabled;
}
