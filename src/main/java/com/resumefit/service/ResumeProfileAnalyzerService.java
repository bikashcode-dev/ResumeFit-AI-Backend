package com.resumefit.service;

import com.resumefit.dto.SectionPriorityDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ResumeProfileAnalyzerService {

    public String detectCandidateStage(String jobDescription, String roleType, String resumeText) {
        String combined = (nullToEmpty(jobDescription) + " " + nullToEmpty(roleType)).toLowerCase(Locale.ROOT);
        String resume = nullToEmpty(resumeText).toLowerCase(Locale.ROOT);

        if (combined.contains("intern")) {
            return "Intern";
        }
        if (combined.contains("fresher") || combined.contains("entry level") || combined.contains("entry-level")) {
            return "Fresher";
        }
        if (combined.contains("senior") || combined.contains("lead") || combined.contains("3+ years") || combined.contains("5+ years")) {
            return "Experienced";
        }
        if (resume.contains("work experience") || resume.contains("professional experience")) {
            return "Experienced";
        }
        if (resume.contains("internship") || resume.contains("internships")) {
            return "Intern";
        }
        return "Fresher";
    }

    public String resolveFinalCandidateStage(String selectedCandidateStage, String detectedCandidateStage) {
        if (selectedCandidateStage == null || selectedCandidateStage.isBlank()) {
            return detectedCandidateStage;
        }
        return selectedCandidateStage.trim();
    }

    public String mismatchWarning(String selectedCandidateStage, String detectedCandidateStage) {
        if (selectedCandidateStage == null || selectedCandidateStage.isBlank() || detectedCandidateStage == null || detectedCandidateStage.isBlank()) {
            return "";
        }
        if (selectedCandidateStage.equalsIgnoreCase(detectedCandidateStage)) {
            return "";
        }
        return "You selected " + selectedCandidateStage + ", but the JD/resume signals look closer to " + detectedCandidateStage + ". Final resume will follow your selection.";
    }

    public List<SectionPriorityDto> recommendOrder(String candidateStage, String roleType) {
        List<SectionPriorityDto> items = new ArrayList<>();
        String stage = candidateStage == null ? "Fresher" : candidateStage;
        String role = nullToEmpty(roleType).toLowerCase(Locale.ROOT);

        items.add(item("HEADER", 1, "Contact details and profile identity should stay first."));
        items.add(item("SUMMARY", 2, "Short role-fit summary helps recruiter scan faster."));
        items.add(item("SKILLS", 3, "ATS checks core tools, languages, frameworks, and platforms early."));

        if ("Experienced".equalsIgnoreCase(stage)) {
            items.add(item("EXPERIENCE", 4, "Experienced profiles should prove delivery and ownership early."));
            items.add(item("PROJECTS", 5, "Projects should support specialization and depth."));
            items.add(item("EDUCATION", 6, "Education supports, but usually sits below work proof."));
        } else if ("Intern".equalsIgnoreCase(stage)) {
            items.add(item("PROJECTS", 4, "Projects usually carry the strongest proof for intern roles."));
            items.add(item("EDUCATION", 5, "Education and coursework matter for early-career evaluation."));
            items.add(item("INTERNSHIP / TRAINING", 6, "Include if you have real training, internship, or applied exposure."));
            items.add(item("CERTIFICATIONS", 7, "Certifications can support fundamentals when experience is limited."));
        } else {
            items.add(item("PROJECTS", 4, "Projects are the clearest proof for fresher profiles."));
            items.add(item("EDUCATION", 5, "Education is still a major signal for fresher hiring."));
            items.add(item("CERTIFICATIONS", 6, "Certifications and coursework can strengthen ATS alignment."));
            items.add(item("SOFT SKILLS", 7, "Only include if they are supported by projects, teamwork, or results."));
        }

        if (role.contains("backend") || role.contains("java")) {
            items.add(item("BACKEND EMPHASIS", 8, "Prioritize APIs, databases, debugging, testing, and reliability in bullets."));
        } else if (role.contains("full")) {
            items.add(item("FULL STACK EMPHASIS", 8, "Balance frontend and backend proof without weakening either side."));
        } else if (role.contains("frontend")) {
            items.add(item("FRONTEND EMPHASIS", 8, "Prioritize UI, responsiveness, accessibility, and integration details."));
        }

        return items;
    }

    public List<String> recommendedOrderNames(String candidateStage, String roleType) {
        return recommendOrder(candidateStage, roleType).stream()
                .map(SectionPriorityDto::getSection)
                .toList();
    }

    private SectionPriorityDto item(String section, int priority, String reason) {
        return SectionPriorityDto.builder()
                .section(section)
                .priority(priority)
                .reason(reason)
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
