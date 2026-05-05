package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeParseResponse {
    String fileName;
    String fileType;
    String uploadMode;
    int pageCount;
    int characterCount;
    String cleanText;
    List<ResumeSectionDto> sections;
    ResumeTemplateProfileDto templateProfile;
    List<String> warnings;
}
