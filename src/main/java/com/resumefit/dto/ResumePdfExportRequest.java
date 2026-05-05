package com.resumefit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumePdfExportRequest {

    @NotBlank(message = "Resume text is required.")
    @Size(min = 40, max = 40000, message = "Resume text must be between 40 and 40000 characters.")
    private String resumeText;

    @Size(max = 120, message = "File name must be under 120 characters.")
    private String fileName;

    private ResumeTemplateProfileDto templateProfile;

    @Size(max = 120, message = "Document title must be under 120 characters.")
    private String documentTitle;
}
