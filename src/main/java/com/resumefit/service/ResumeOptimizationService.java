package com.resumefit.service;

import com.resumefit.dto.KeywordMatchDto;
import com.resumefit.dto.ResumeMatchRequest;
import com.resumefit.dto.ResumeMatchResponse;
import com.resumefit.dto.ResumeOptimizeRequest;
import com.resumefit.dto.ResumeOptimizeResponse;
import com.resumefit.util.ResumeSectionEditor;
import com.resumefit.util.ResumeTruthGuard;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResumeOptimizationService {

    private final ResumeMatchingService matchingService;
    private final AiPromptBuilder promptBuilder;
    private final ResumeAiService aiClient;
    private final ResumeProfileAnalyzerService profileAnalyzerService;

    public ResumeOptimizationService(
            ResumeMatchingService matchingService,
            AiPromptBuilder promptBuilder,
            ResumeAiService aiClient,
            ResumeProfileAnalyzerService profileAnalyzerService
    ) {
        this.matchingService = matchingService;
        this.promptBuilder = promptBuilder;
        this.aiClient = aiClient;
        this.profileAnalyzerService = profileAnalyzerService;
    }

    public ResumeOptimizeResponse optimize(ResumeOptimizeRequest request) {
        ResumeMatchResponse match = matchingService.match(new ResumeMatchRequest(
                request.getResumeText(),
                request.getJobDescription(),
                request.getSkills(),
                request.getCandidateStage()
        ));

        List<String> missingKeywords = match.getMissingKeywords()
                .stream()
                .filter(keyword -> !"general".equals(keyword.getCategory()))
                .limit(12)
                .map(KeywordMatchDto::getKeyword)
                .toList();

        String candidateStage = profileAnalyzerService.detectCandidateStage(
                request.getJobDescription(),
                request.getRoleType(),
                request.getResumeText()
        );
        String finalCandidateStage = profileAnalyzerService.resolveFinalCandidateStage(request.getCandidateStage(), candidateStage);
        String profileMismatchWarning = profileAnalyzerService.mismatchWarning(request.getCandidateStage(), candidateStage);
        List<String> recommendedOrder = profileAnalyzerService.recommendedOrderNames(finalCandidateStage, request.getRoleType());

        String prompt = promptBuilder.buildOptimizationPrompt(
                request.getResumeText(),
                request.getJobDescription(),
                request.getRoleType(),
                finalCandidateStage,
                request.getSkills(),
                missingKeywords,
                recommendedOrder
        );

        return aiClient.complete(prompt)
                .map(optimizedResume -> ResumeTruthGuard.keepOnlySafeOutput(
                        optimizedResume,
                        request.getResumeText(),
                        request.getSkills(),
                        request.getJobDescription()
                ))
                .filter(safeResume -> !safeResume.isBlank())
                .map(optimizedResume -> ResumeOptimizeResponse.builder()
                        .optimizedResume(optimizedResume)
                        .aiGenerated(true)
                        .selectedCandidateStage(finalCandidateStage)
                        .detectedCandidateStage(candidateStage)
                        .profileMismatchWarning(profileMismatchWarning)
                        .recommendedOrder(recommendedOrder)
                        .appliedRules(buildRules(true))
                        .build())
                .orElseGet(() -> ResumeOptimizeResponse.builder()
                        .optimizedResume(buildFallbackResume(
                                request.getResumeText(),
                                request.getRoleType(),
                                finalCandidateStage,
                                request.getSkills(),
                                match.getMatchedKeywords().stream().map(KeywordMatchDto::getKeyword).toList(),
                                missingKeywords
                        ))
                        .aiGenerated(false)
                        .selectedCandidateStage(finalCandidateStage)
                        .detectedCandidateStage(candidateStage)
                        .profileMismatchWarning(profileMismatchWarning)
                        .recommendedOrder(recommendedOrder)
                        .appliedRules(buildRules(false))
                        .build());
    }

    private String buildFallbackResume(
            String resumeText,
            String roleType,
            String candidateStage,
            String skills,
            List<String> matchedKeywords,
            List<String> missingKeywords
    ) {
        return ResumeSectionEditor.optimizePlainTextResume(
                resumeText,
                roleType,
                candidateStage,
                splitSkills(skills),
                matchedKeywords,
                missingKeywords
        );
    }

    private List<String> buildRules(boolean aiGenerated) {
        return List.of(
                aiGenerated ? "Draft refined using the resume improvement pipeline." : "Standard draft returned because the external model was unavailable.",
                "Preserve original section order.",
                "Do not invent experience.",
                "Avoid unsupported skills and missing core headings.",
                "Add missing keywords only when they fit naturally.",
                "Prefer measurable bullet points."
        );
    }

    private List<String> splitSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return List.of();
        }

        return List.of(skills.split("[,|]"))
                .stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }
}
