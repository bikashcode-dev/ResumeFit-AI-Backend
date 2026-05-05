package com.resumefit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptionAnalyzeRequest {

    @NotBlank(message = "Job description is required.")
    @Size(min = 80, max = 12000, message = "Job description must be between 80 and 12000 characters.")
    private String jobDescription;
}
