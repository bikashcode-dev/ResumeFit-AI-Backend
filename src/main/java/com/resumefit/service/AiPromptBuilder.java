package com.resumefit.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiPromptBuilder {

    public String buildOptimizationPrompt(
            String resumeText,
            String jobDescription,
            String roleType,
            String candidateStage,
            String skills,
            List<String> missingKeywords,
            List<String> recommendedOrder
    ) {
        return """
                You are an expert ATS resume editor.

                Task:
                Optimize the resume for the given job description and return a stronger, more role-aligned version.

                Non-negotiable rules:
                - Preserve the original section structure and section order.
                - Do not invent companies, dates, degrees, certifications, achievements, or experience.
                - Rewrite the summary to align to the JD using only real skills and real evidence from the source resume.
                - Improve weak bullet points using action verbs, tools, and outcomes, but keep every bullet grounded in the source content.
                - Reorder bullets inside EXPERIENCE and PROJECTS so the most JD-relevant bullets come first.
                - Keep SKILLS focused on candidate-confirmed skills and source-supported technologies.
                - Add missing JD keywords only when they naturally fit the candidate's existing content and confirmed skills.
                - Keep the resume ATS-friendly with simple headings and plain text.
                - Prefer recruiter-ready wording over generic filler.
                - For fresher or intern profiles, prioritize projects, education, practical exposure, Git, testing, debugging, and role-relevant fundamentals.
                - For backend roles, prioritize APIs, databases, server-side tools, debugging, and reliability.
                - For full stack roles, balance frontend and backend evidence without overclaiming.
                - Return only the optimized resume text, no explanation.

                Candidate stage:
                %s

                Selected role:
                %s

                Recommended section order:
                %s

                Missing JD keywords to consider:
                %s

                Candidate-confirmed skills:
                %s

                Output quality bar:
                - Replace generic summary language with concrete role-fit language.
                - Do not add placeholder guidance like "highlight", "consider", or "add if truthful".
                - Final output must read like a finished resume, not notes.

                Resume:
                %s

                Job Description:
                %s
                """.formatted(
                candidateStage == null ? "" : candidateStage,
                roleType == null ? "" : roleType,
                String.join(" -> ", recommendedOrder == null ? List.of() : recommendedOrder),
                String.join(", ", missingKeywords),
                skills == null ? "" : skills,
                resumeText,
                jobDescription
        );
    }

    public String buildVersionPrompt(
            String resumeText,
            String jobDescription,
            String skills,
            String roleType,
            String candidateStage,
            List<String> roleGuidelines,
            List<String> recommendedOrder
    ) {
        return """
                You are an expert resume writer.

                Task:
                Generate a %s resume version from the source resume.

                Rules:
                - Keep all facts consistent with the source resume.
                - Preserve core identity, education, project truth, and work history.
                - Adjust tone, summary, skills ordering, and bullet emphasis for the target role.
                - Reorder sections and bullets to fit the candidate stage and ATS expectations.
                - Do not add fake experience or technologies.
                - Keep output ATS-friendly and plain text.
                - Return only the role-based resume text.

                Candidate stage:
                %s

                Recommended section order:
                %s

                Role guidelines:
                %s

                Candidate-confirmed skills:
                %s

                Source resume:
                %s

                Optional JD context:
                %s
                """.formatted(
                roleType,
                candidateStage == null ? "" : candidateStage,
                String.join(" -> ", recommendedOrder == null ? List.of() : recommendedOrder),
                String.join("\n", roleGuidelines),
                skills == null ? "" : skills,
                resumeText,
                jobDescription == null ? "" : jobDescription
        );
    }

    public String buildSectionAssistPrompt(
            String sectionType,
            String currentContent,
            String roleType,
            String candidateLevel,
            String skills
    ) {
        return """
                You are an expert resume editor improving one resume section.

                Task:
                Improve the given section for ATS readability and recruiter quality.

                Rules:
                - Return only the improved section content, not the heading.
                - Do not invent any company, metric, tool, certification, achievement, or claim.
                - Keep every line grounded in the provided content and candidate-confirmed skills.
                - For summary: write 2-4 sharp lines, role-aligned and truthful.
                - For projects or experience: use concise action-led bullets with tools and outcomes when supported.
                - For skills: group and clean the skills into ATS-friendly lines only from provided content and confirmed skills.
                - Keep the tone aligned to the candidate level and target role.
                - Avoid filler like "I am responsible for" or "hardworking individual".

                Candidate level:
                %s

                Target role:
                %s

                Candidate-confirmed skills:
                %s

                Section type:
                %s

                Current section content:
                %s
                """.formatted(
                candidateLevel == null ? "" : candidateLevel,
                roleType == null ? "" : roleType,
                skills == null ? "" : skills,
                sectionType == null ? "" : sectionType,
                currentContent == null ? "" : currentContent
        );
    }
}
