package com.resumefit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeMatchRequest {

    @NotBlank(message = "Resume text is required.")
    @Size(min = 80, max = 12000, message = "Resume text must be between 80 and 12000 characters.")
    private String resumeText;

    @NotBlank(message = "Job description is required.")
    @Size(min = 80, max = 12000, message = "Job description must be between 80 and 12000 characters.")
    private String jobDescription;

    @Size(max = 2000, message = "Skills text must be under 2000 characters.")
    private String skills;

    @Size(max = 80, message = "Candidate stage must be under 80 characters.")
    private String candidateStage;
}
