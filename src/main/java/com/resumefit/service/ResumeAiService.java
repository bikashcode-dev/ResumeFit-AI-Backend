package com.resumefit.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ResumeAiService {

    private final GeminiResumeClient geminiResumeClient;
    private final OpenRouterResumeClient openRouterResumeClient;
    private final OpenAiResumeClient openAiResumeClient;

    public ResumeAiService(
            GeminiResumeClient geminiResumeClient,
            OpenRouterResumeClient openRouterResumeClient,
            OpenAiResumeClient openAiResumeClient
    ) {
        this.geminiResumeClient = geminiResumeClient;
        this.openRouterResumeClient = openRouterResumeClient;
        this.openAiResumeClient = openAiResumeClient;
    }

    public Optional<String> complete(String prompt) {
        Optional<String> geminiResult = geminiResumeClient.complete(prompt);
        if (geminiResult.isPresent()) {
            return geminiResult;
        }
        Optional<String> openRouterResult = openRouterResumeClient.complete(prompt);
        if (openRouterResult.isPresent()) {
            return openRouterResult;
        }
        return openAiResumeClient.complete(prompt);
    }
}
