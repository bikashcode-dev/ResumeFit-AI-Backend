package com.resumefit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:https://roleforgeai.netlify.app," +
            "https://ai-resumeforge.netlify.app," +
            "https://astounding-stardust-efd692.netlify.app," +
            "http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-origin:}")
    private String legacyAllowedOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(resolveAllowedOrigins())
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }

    private String[] resolveAllowedOrigins() {
        String combinedOrigins = allowedOrigins + "," + legacyAllowedOrigin;
        return java.util.Arrays.stream(combinedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toArray(String[]::new);
    }
}
