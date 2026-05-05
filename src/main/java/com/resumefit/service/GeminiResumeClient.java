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
public class GeminiResumeClient {

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public GeminiResumeClient(
            ObjectMapper objectMapper,
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-2.5-flash}") String model,
            @Value("${ai.gemini.url:https://generativelanguage.googleapis.com/v1beta/models}") String apiUrl
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
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            String response = RestClient.create()
                    .post()
                    .uri(apiUrl + "/" + model + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            return content == null || content.isBlank() ? Optional.empty() : Optional.of(content.trim());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
