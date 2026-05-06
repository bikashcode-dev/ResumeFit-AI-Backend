package com.resumefit.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ResumeAiService {

    private final GeminiResumeClient geminiResumeClient;
    private final OpenRouterResumeClient openRouterResumeClient;

    public ResumeAiService(
            GeminiResumeClient geminiResumeClient,
            OpenRouterResumeClient openRouterResumeClient
    ) {
        this.geminiResumeClient = geminiResumeClient;
        this.openRouterResumeClient = openRouterResumeClient;
    }

    public Optional<String> complete(String prompt) {
        Optional<String> geminiResult = geminiResumeClient.complete(prompt);
        if (geminiResult.isPresent()) {
            return geminiResult;
        }

        return openRouterResumeClient.complete(prompt);
    }
}
