package com.resumefit.service;

import com.resumefit.dto.AtsAnalysis;
import com.resumefit.dto.ResumeMetadata;
import com.resumefit.dto.ResumeTailorRequest;
import com.resumefit.dto.ResumeTailorResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ResumeTailoringService {

    private static final int MAX_KEYWORDS = 24;
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");
    private static final Pattern SKILL_SPLIT_PATTERN = Pattern.compile("[,|]");
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "also", "and", "are", "based", "can", "for", "from",
            "has", "have", "into", "job", "our", "role", "that", "the", "their",
            "this", "with", "will", "work", "your"
    );
    private static final List<String> ATS_SECTION_HINTS = List.of(
            "summary", "profile", "skills", "experience", "projects", "education", "certifications"
    );

    public ResumeTailorResponse tailor(ResumeTailorRequest request) {
        List<String> skills = splitSkills(request.skills());
        List<String> jobKeywords = extractKeywords(request.jobDescription());
        List<String> resumeKeywords = extractKeywords(request.resumeText());
        List<String> matchedKeywords = jobKeywords.stream()
                .filter(resumeKeywords::contains)
                .toList();
        List<String> missingKeywords = jobKeywords.stream()
                .filter(keyword -> !resumeKeywords.contains(keyword))
                .toList();
        int atsScore = calculateAtsScore(jobKeywords, matchedKeywords, request.resumeText());

        AtsAnalysis ats = new AtsAnalysis(
                atsScore,
                matchedKeywords,
                missingKeywords.stream().limit(12).toList(),
                buildRecommendations(atsScore, missingKeywords)
        );

        return new ResumeTailorResponse(
                buildTailoredResume(request.resumeText(), skills, jobKeywords, missingKeywords),
                ats,
                buildRecruiterNotes(request.jobDescription(), skills),
                new ResumeMetadata(
                        "spring-boot-heuristic-tailor-v1",
                        "Original resume order is preserved, with ATS-aligned sections appended for manual review."
                )
        );
    }

    private String buildTailoredResume(
            String resumeText,
            List<String> skills,
            List<String> jobKeywords,
            List<String> missingKeywords
    ) {
        String summary = buildSummary(jobKeywords, skills);
        String skillLine = buildSkillLine(skills, jobKeywords);
        List<String> bulletOptions = buildExperienceBullets(jobKeywords, missingKeywords);

        return String.join("\n",
                resumeText.trim(),
                "",
                "TAILORED SUMMARY",
                summary,
                "",
                "ATS-ALIGNED SKILLS",
                skillLine,
                "",
                "EXPERIENCE BULLET OPTIONS",
                bulletOptions.stream().map(bullet -> "- " + bullet).collect(Collectors.joining("\n"))
        );
    }

    private String buildSummary(List<String> jobKeywords, List<String> skills) {
        String primaryKeywords = String.join(", ", jobKeywords.stream().limit(5).toList());
        String primarySkills = String.join(", ", skills.stream().limit(4).toList());

        if (primaryKeywords.isBlank() && primarySkills.isBlank()) {
            return "Results-focused professional with practical experience aligned to the target role, strong ownership, and a focus on measurable business outcomes.";
        }

        String experienceFocus = primarySkills.isBlank() ? primaryKeywords : primarySkills;
        String roleNeeds = primaryKeywords.isBlank() ? primarySkills : primaryKeywords;

        return "Results-focused professional with experience across " + experienceFocus
                + ". Strong fit for roles requiring " + roleNeeds
                + ", with a track record of translating requirements into reliable, business-ready outcomes.";
    }

    private String buildSkillLine(List<String> skills, List<String> jobKeywords) {
        LinkedHashSet<String> mergedSkills = new LinkedHashSet<>(skills);
        jobKeywords.stream().limit(10).forEach(mergedSkills::add);

        if (mergedSkills.isEmpty()) {
            return "Add role-specific tools, technologies, and business skills here.";
        }

        return String.join(" | ", mergedSkills);
    }

    private List<String> buildExperienceBullets(List<String> jobKeywords, List<String> missingKeywords) {
        List<String> focusKeywords = new ArrayList<>();
        focusKeywords.addAll(jobKeywords.stream().limit(5).toList());
        focusKeywords.addAll(missingKeywords.stream().limit(3).toList());

        if (focusKeywords.isEmpty()) {
            return List.of(
                    "Improved delivery quality by aligning responsibilities with business requirements and measurable outcomes.",
                    "Collaborated with stakeholders to complete tasks accurately, on time, and with clear communication."
            );
        }

        String firstThree = String.join(", ", focusKeywords.stream().limit(3).toList());
        String middleThree = String.join(", ", focusKeywords.stream().skip(1).limit(3).toList());

        return List.of(
                "Applied " + firstThree + " to deliver role-aligned solutions and improve execution quality.",
                "Collaborated with cross-functional teams to translate " + focusKeywords.get(0) + " requirements into clear, maintainable deliverables.",
                "Strengthened resume alignment by highlighting hands-on experience in " + middleThree + " where truthful and relevant."
        );
    }

    private List<String> buildRecommendations(int score, List<String> missingKeywords) {
        List<String> recommendations = new ArrayList<>();

        if (score < 70) {
            recommendations.add("Add more truthful JD keywords inside Summary, Skills, Projects, and Experience sections.");
        }

        if (!missingKeywords.isEmpty()) {
            recommendations.add("Review missing keywords before applying: "
                    + String.join(", ", missingKeywords.stream().limit(6).toList()) + ".");
        }

        recommendations.add("Keep headings simple and ATS-readable: Summary, Skills, Experience, Projects, Education.");
        recommendations.add("Avoid adding skills or experience that you cannot defend in an interview.");
        return recommendations;
    }

    private List<String> buildRecruiterNotes(String jobDescription, List<String> skills) {
        String roleType = inferRoleType(jobDescription);
        String strongestSkills = String.join(", ", skills.stream().limit(5).toList());

        List<String> notes = new ArrayList<>();
        notes.add("Positioning angle: present yourself as a " + roleType + " candidate with evidence from projects and work experience.");
        notes.add(strongestSkills.isBlank()
                ? "Manual recruiter focus: add your strongest role-specific skills near the top of the resume."
                : "Manual recruiter focus: make " + strongestSkills + " visible in the top half of the resume.");
        notes.add("Before final submission, replace generic bullet options with exact numbers, tools, project names, and outcomes from your real work.");
        return notes;
    }

    private String inferRoleType(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);

        if (lowerText.contains("frontend") || lowerText.contains("react")) {
            return "frontend";
        }
        if (lowerText.contains("backend") || lowerText.contains("api")) {
            return "backend";
        }
        if (lowerText.contains("data") || lowerText.contains("analytics")) {
            return "data";
        }
        if (lowerText.contains("full stack") || lowerText.contains("full-stack")) {
            return "full-stack";
        }

        return "role-aligned";
    }

    private int calculateAtsScore(List<String> jobKeywords, List<String> matchedKeywords, String resumeText) {
        if (jobKeywords.isEmpty()) {
            return 55;
        }

        int keywordScore = Math.round((matchedKeywords.size() / (float) jobKeywords.size()) * 70);
        String lowerResume = resumeText.toLowerCase(Locale.ROOT);
        int structureScore = ATS_SECTION_HINTS.stream()
                .mapToInt(section -> lowerResume.contains(section) ? 5 : 0)
                .sum();

        return Math.min(100, keywordScore + Math.min(30, structureScore));
    }

    private List<String> extractKeywords(String text) {
        Map<String, Long> frequency = WORD_SPLIT_PATTERN.splitAsStream(cleanText(text))
                .map(String::trim)
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return frequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_KEYWORDS)
                .map(Map.Entry::getKey)
                .toList();
    }

    private String cleanText(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.\\s-]", " ");
    }

    private List<String> splitSkills(String skills) {
        return SKILL_SPLIT_PATTERN.splitAsStream(skills)
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .distinct()
                .toList();
    }
}
