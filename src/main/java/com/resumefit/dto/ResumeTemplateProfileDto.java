package com.resumefit.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeTemplateProfileDto {
    String layoutType;
    String visualStyle;
    String spacingStyle;
    String headingStyle;
    List<String> sectionOrder;
}
