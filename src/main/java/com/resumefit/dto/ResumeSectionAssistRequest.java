package com.resumefit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSectionAssistRequest {

    @NotBlank(message = "Section type is required.")
    @Size(max = 80, message = "Section type must be under 80 characters.")
    private String sectionType;

    @NotBlank(message = "Current content is required.")
    @Size(max = 7000, message = "Current content must be under 7000 characters.")
    private String currentContent;

    @NotBlank(message = "Role type is required.")
    @Size(max = 120, message = "Role type must be under 120 characters.")
    private String roleType;

    @NotBlank(message = "Candidate level is required.")
    @Size(max = 80, message = "Candidate level must be under 80 characters.")
    private String candidateLevel;

    @Size(max = 3000, message = "Skills must be under 3000 characters.")
    private String skills;
}
