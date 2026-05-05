package com.resumefit.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.resumefit.dto.ResumeTailorRequest;
import com.resumefit.dto.ResumeTailorResponse;
import org.junit.jupiter.api.Test;

class ResumeTailoringServiceTest {

    private final ResumeTailoringService service = new ResumeTailoringService();

    @Test
    void tailorReturnsAtsAnalysisAndResumeOutput() {
        ResumeTailorRequest request = new ResumeTailorRequest(
                "Summary\nBackend developer with Spring Boot, REST API, SQL, testing, and deployment experience across business applications.",
                "We need a backend developer with Spring Boot, REST API, SQL, testing, documentation, and deployment ownership for scalable services.",
                "Spring Boot, REST API, SQL"
        );

        ResumeTailorResponse response = service.tailor(request);

        assertThat(response.tailoredResume()).contains("TAILORED SUMMARY");
        assertThat(response.ats().score()).isBetween(1, 100);
        assertThat(response.ats().matchedKeywords()).contains("spring", "boot");
    }
}
