package com.resumefit.service;

import com.resumefit.dto.SectionPriorityDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ResumeSectionPriorityService {

    private final ResumeProfileAnalyzerService profileAnalyzerService;

    public ResumeSectionPriorityService(ResumeProfileAnalyzerService profileAnalyzerService) {
        this.profileAnalyzerService = profileAnalyzerService;
    }

    public ResumeProfileAnalyzerService getProfileAnalyzerService() {
        return profileAnalyzerService;
    }

    public List<SectionPriorityDto> recommend(String jobDescription) {
        String normalized = jobDescription == null ? "" : jobDescription.toLowerCase(Locale.ROOT);
        String stage = normalized.contains("intern") ? "Intern"
                : normalized.contains("fresher") || normalized.contains("entry level") || normalized.contains("entry-level") ? "Fresher"
                : "Experienced";
        String role = normalized.contains("frontend") || normalized.contains("react") ? "Frontend Developer"
                : normalized.contains("full stack") || normalized.contains("full-stack") ? "Full Stack Developer"
                : normalized.contains("backend") || normalized.contains("spring") || normalized.contains("api") ? "Backend Developer"
                : "General";
        return profileAnalyzerService.recommendOrder(stage, role);
    }

    private SectionPriorityDto priority(String section, int priority, String reason) {
        return SectionPriorityDto.builder()
                .section(section)
                .priority(priority)
                .reason(reason)
                .build();
    }
}
