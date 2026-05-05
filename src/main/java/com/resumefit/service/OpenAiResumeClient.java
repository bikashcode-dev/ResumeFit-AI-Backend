package com.resumefit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiResumeClient {

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public OpenAiResumeClient(
            ObjectMapper objectMapper,
            @Value("${ai.openai.api-key:}") String apiKey,
            @Value("${ai.openai.model:gpt-4o-mini}") String model,
            @Value("${ai.openai.url:https://api.openai.com/v1/chat/completions}") String apiUrl
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
    }

    public Optional<String> complete(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", 0.3,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You write truthful, ATS-friendly resumes."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String response = RestClient.create()
                    .post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return content == null || content.isBlank() ? Optional.empty() : Optional.of(content.trim());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
