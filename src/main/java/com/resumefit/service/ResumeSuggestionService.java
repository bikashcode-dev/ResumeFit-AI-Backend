package com.resumefit.service;

import com.resumefit.dto.KeywordMatchDto;
import com.resumefit.dto.ResumeMatchRequest;
import com.resumefit.dto.ResumeMatchResponse;
import com.resumefit.dto.ResumeSuggestionResponse;
import com.resumefit.dto.SuggestionDto;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ResumeSuggestionService {

    private static final Pattern METRIC_PATTERN = Pattern.compile("\\b\\d+(%|\\+|x|k|m)?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern BULLET_PATTERN = Pattern.compile("(?m)^\\s*[-*]\\s+.+$");

    private final ResumeMatchingService matchingService;

    public ResumeSuggestionService(ResumeMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    public ResumeSuggestionResponse suggest(ResumeMatchRequest request) {
        ResumeMatchResponse match = matchingService.match(request);
        List<SuggestionDto> suggestions = new ArrayList<>();

        addMissingSkillSuggestions(match, suggestions);
        addWeakBulletSuggestions(request.getResumeText(), suggestions);
        addMetricSuggestions(request.getResumeText(), suggestions);
        addTruthSuggestions(match, suggestions);
        addSectionOrderSuggestions(match, suggestions);

        if (suggestions.isEmpty()) {
            suggestions.add(SuggestionDto.builder()
                    .type("quality")
                    .priority("low")
                    .title("Resume is already well aligned")
                    .action("Do a final manual review for grammar, truthful claims, and exact project outcomes.")
                    .reason("No major rule-based gaps were detected.")
                    .build());
        }

        return ResumeSuggestionResponse.builder()
                .matchScore(match.getMatchScore())
                .suggestions(suggestions)
                .build();
    }

    private void addMissingSkillSuggestions(ResumeMatchResponse match, List<SuggestionDto> suggestions) {
        List<String> missingImportant = match.getMissingKeywords()
                .stream()
                .filter(keyword -> !"general".equals(keyword.getCategory()))
                .limit(8)
                .map(KeywordMatchDto::getKeyword)
                .toList();

        if (!missingImportant.isEmpty()) {
            suggestions.add(SuggestionDto.builder()
                    .type("missing_skills")
                    .priority("high")
                    .title("Add truthful missing JD keywords")
                    .action("If you genuinely know them, add these keywords in Skills, Projects, or Experience: " + String.join(", ", missingImportant) + ".")
                    .reason("These are weighted JD keywords, so they affect both ATS matching and recruiter scanning.")
                    .build());
        }
    }

    private void addWeakBulletSuggestions(String resumeText, List<SuggestionDto> suggestions) {
        long bulletCount = BULLET_PATTERN.matcher(resumeText).results().count();

        if (bulletCount < 4) {
            suggestions.add(SuggestionDto.builder()
                    .type("weak_bullets")
                    .priority("medium")
                    .title("Add stronger experience or project bullets")
                    .action("Use action + tool + outcome format, for example: Built REST APIs with Spring Boot to reduce manual data entry time.")
                    .reason("Short resumes or paragraph-only resumes are harder for ATS and recruiters to scan quickly.")
                    .build());
        }
    }

    private void addMetricSuggestions(String resumeText, List<SuggestionDto> suggestions) {
        if (!METRIC_PATTERN.matcher(resumeText).find()) {
            suggestions.add(SuggestionDto.builder()
                    .type("lack_of_metrics")
                    .priority("medium")
                    .title("Add measurable outcomes")
                    .action("Add numbers where truthful: users served, response time improved, bugs reduced, pages built, APIs created, or project duration.")
                    .reason("Metrics make bullet points more credible and help manual recruiters understand impact.")
                    .build());
        }
    }

    private void addTruthSuggestions(ResumeMatchResponse match, List<SuggestionDto> suggestions) {
        if (match.getTruthAnalysis().getTruthScore() < 75) {
            suggestions.add(SuggestionDto.builder()
                    .type("truth_risk")
                    .priority("high")
                    .title("Strengthen proof for your claims")
                    .action("Support JD-facing skills with project, internship, or work bullets before adding more keywords.")
                    .reason("Truth score is low, which means the resume has skill gaps or weak evidence for some important terms.")
                    .build());
        }
    }

    private void addSectionOrderSuggestions(ResumeMatchResponse match, List<SuggestionDto> suggestions) {
        if (!match.getSectionPriorities().isEmpty()) {
            suggestions.add(SuggestionDto.builder()
                    .type("section_order")
                    .priority("medium")
                    .title("Review section order")
                    .action("Recommended order for this role: " + String.join(" > ", match.getSectionPriorities().stream().limit(5).map(priority -> priority.getSection()).toList()) + ".")
                    .reason("Better section ordering helps both ATS scanning and recruiter readability.")
                    .build());
        }
    }
}
