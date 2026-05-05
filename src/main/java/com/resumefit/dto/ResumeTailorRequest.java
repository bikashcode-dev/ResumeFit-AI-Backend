package com.resumefit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeTailorRequest(
        @NotBlank(message = "Resume text is required.")
        @Size(min = 80, max = 12000, message = "Resume text must be between 80 and 12000 characters.")
        String resumeText,

        @NotBlank(message = "Job description is required.")
        @Size(min = 80, max = 12000, message = "Job description must be between 80 and 12000 characters.")
        String jobDescription,

        @NotBlank(message = "Skills are required.")
        @Size(max = 2000, message = "Skills text must be under 2000 characters.")
        String skills
) {
}
