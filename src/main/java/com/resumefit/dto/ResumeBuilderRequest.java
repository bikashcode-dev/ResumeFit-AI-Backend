package com.resumefit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeBuilderRequest {

    @NotBlank(message = "Full name is required.")
    @Size(max = 120, message = "Full name must be under 120 characters.")
    private String fullName;

    @Size(max = 80, message = "Phone number must be under 80 characters.")
    private String phone;

    @Size(max = 120, message = "Email must be under 120 characters.")
    private String email;

    @Size(max = 200, message = "LinkedIn URL must be under 200 characters.")
    private String linkedinUrl;

    @Size(max = 200, message = "GitHub URL must be under 200 characters.")
    private String githubUrl;

    @Size(max = 200, message = "Portfolio URL must be under 200 characters.")
    private String portfolioUrl;

    @NotBlank(message = "Skills are required.")
    @Size(max = 2000, message = "Skills must be under 2000 characters.")
    private String skills;

    @Size(max = 12000, message = "Job description must be under 12000 characters.")
    private String jobDescription;

    @NotBlank(message = "Role type is required.")
    @Size(max = 120, message = "Role type must be under 120 characters.")
    private String roleType;

    @NotBlank(message = "Candidate level is required.")
    @Size(max = 80, message = "Candidate level must be under 80 characters.")
    private String candidateLevel;

    @Size(max = 120, message = "Current location must be under 120 characters.")
    private String currentLocation;

    @Size(max = 2500, message = "Summary must be under 2500 characters.")
    private String summary;

    @Size(max = 5000, message = "Education details must be under 5000 characters.")
    private String educationDetails;

    @Size(max = 7000, message = "Project details must be under 7000 characters.")
    private String projectDetails;

    @Size(max = 7000, message = "Experience details must be under 7000 characters.")
    private String experienceDetails;

    @Size(max = 3000, message = "Achievements must be under 3000 characters.")
    private String achievements;

    @Size(max = 3000, message = "Certifications must be under 3000 characters.")
    private String certifications;

    private List<String> sectionOrder;
    private List<ResumeCustomSectionDto> customSections;

    private Boolean includeSummary;
    private Boolean includeSkills;
    private Boolean includeProjects;
    private Boolean includeEducation;
    private Boolean includeExperience;
    private Boolean includeCertifications;
    private Boolean includeAchievements;
}
