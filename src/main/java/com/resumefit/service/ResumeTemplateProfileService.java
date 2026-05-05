package com.resumefit.service;

import com.resumefit.dto.ResumeSectionDto;
import com.resumefit.dto.ResumeTemplateProfileDto;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ResumeTemplateProfileService {

    public ResumeTemplateProfileDto buildProfile(String cleanText, List<ResumeSectionDto> sections, int pageCount, String fileType) {
        List<String> sectionOrder = sections.stream()
                .map(ResumeSectionDto::getTitle)
                .toList();

        String normalized = cleanText == null ? "" : cleanText.toLowerCase(Locale.ROOT);
        double averageLineLength = cleanText == null || cleanText.isBlank()
                ? 0
                : cleanText.lines().map(String::trim).filter(line -> !line.isBlank()).mapToInt(String::length).average().orElse(0);

        String layoutType = inferLayout(normalized, averageLineLength, pageCount);
        String visualStyle = inferVisualStyle(normalized, fileType);
        String spacingStyle = averageLineLength > 75 ? "compact" : "comfortable";
        String headingStyle = sectionOrder.stream().anyMatch(title -> title.equals(title.toUpperCase(Locale.ROOT)))
                ? "uppercase"
                : "title-case";

        return ResumeTemplateProfileDto.builder()
                .layoutType(layoutType)
                .visualStyle(visualStyle)
                .spacingStyle(spacingStyle)
                .headingStyle(headingStyle)
                .sectionOrder(sectionOrder)
                .build();
    }

    private String inferLayout(String normalized, double averageLineLength, int pageCount) {
        if (normalized.contains("|") || normalized.contains("  ") || averageLineLength < 34) {
            return "two-column-inspired";
        }
        if (pageCount > 1 || averageLineLength > 75) {
            return "single-column-dense";
        }
        return "single-column";
    }

    private String inferVisualStyle(String normalized, String fileType) {
        if (normalized.contains("profile") || normalized.contains("summary")) {
            return "minimal-professional";
        }
        if ("docx".equalsIgnoreCase(fileType)) {
            return "structured-office";
        }
        return "clean-ats";
    }
}
