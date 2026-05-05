package com.resumefit.service;

import com.resumefit.dto.ResumeTemplateProfileDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class ResumePdfExportService {

    public byte[] export(String resumeText, String style, ResumeTemplateProfileDto templateProfile, String documentTitle) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDType1Font headingFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            LayoutConfig layoutConfig = layoutFor(style, templateProfile);
            float margin = layoutConfig.margin();
            float pageWidth = PDRectangle.A4.getWidth() - (margin * 2);
            float bottomMargin = 60;
            float headingFontSize = layoutConfig.headingFontSize();
            float bodyFontSize = layoutConfig.bodyFontSize();
            float leading = layoutConfig.leading();

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            float y = page.getMediaBox().getHeight() - margin;

            try {
                List<String> printableLines = new ArrayList<>();
                if (documentTitle != null && !documentTitle.isBlank()) {
                    printableLines.add(documentTitle.strip());
                    printableLines.add("");
                }
                printableLines.addAll(List.of(resumeText.split("\\n")));

                for (String line : printableLines) {
                    String text = line == null ? "" : safePdfText(line.strip());
                    boolean heading = !text.isBlank() && looksLikeHeading(text, templateProfile, documentTitle);
                    boolean title = documentTitle != null && text.equalsIgnoreCase(documentTitle.strip());
                    boolean contactLine = !title && text.contains("|") && text.length() < 160;
                    PDType1Font font = heading ? headingFont : bodyFont;
                    float fontSize = title ? headingFontSize + 4 : heading ? headingFontSize : contactLine ? bodyFontSize - 1 : bodyFontSize;
                    List<String> wrappedLines = wrapText(text, font, fontSize, pageWidth);

                    if (wrappedLines.isEmpty()) {
                        wrappedLines = List.of(" ");
                    }

                    for (String wrappedLine : wrappedLines) {
                        if (y < bottomMargin) {
                            stream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            stream = new PDPageContentStream(document, page);
                            y = page.getMediaBox().getHeight() - margin;
                        }

                        stream.beginText();
                        stream.setFont(font, fontSize);
                        float xOffset = margin;
                        if (title) {
                            xOffset = margin + 10;
                        }
                        if (!heading && wrappedLine.stripLeading().startsWith("-")) {
                            xOffset += 12;
                        }
                        stream.newLineAtOffset(xOffset, y);
                        stream.showText(wrappedLine.isBlank() ? " " : wrappedLine);
                        stream.endText();
                        y -= title ? leading + 6 : heading ? leading + 2 : contactLine ? leading : leading;
                    }
                }
            } finally {
                stream.close();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to export PDF resume.", exception);
        }
    }

    private String safePdfText(String value) {
        return value
                .replace("\t", " ")
                .replace("\u2022", "-")
                .replace("\u2013", "-")
                .replace("\u2014", "-")
                .replace("\u2019", "'")
                .replace("\u201c", "\"")
                .replace("\u201d", "\"")
                .replaceAll("[^\\x20-\\x7E]", " ");
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        if (text == null || text.isBlank()) {
            return List.of("");
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            float candidateWidth = font.getStringWidth(candidate) / 1000 * fontSize;

            if (candidateWidth <= maxWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
                continue;
            }

            lines.add(word);
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private boolean looksLikeHeading(String text, ResumeTemplateProfileDto templateProfile, String documentTitle) {
        if (documentTitle != null && text.equalsIgnoreCase(documentTitle.strip())) {
            return true;
        }
        if (text.equals(text.toUpperCase(Locale.ROOT)) && text.length() < 40) {
            return true;
        }
        if (templateProfile == null || templateProfile.getSectionOrder() == null) {
            return false;
        }
        return templateProfile.getSectionOrder().stream().anyMatch(title -> title.equalsIgnoreCase(text));
    }

    private LayoutConfig layoutFor(String style, ResumeTemplateProfileDto templateProfile) {
        if ("template".equalsIgnoreCase(style) && templateProfile != null) {
            boolean compact = "compact".equalsIgnoreCase(templateProfile.getSpacingStyle());
            boolean dense = "single-column-dense".equalsIgnoreCase(templateProfile.getLayoutType())
                    || "two-column-inspired".equalsIgnoreCase(templateProfile.getLayoutType());
            return new LayoutConfig(
                    dense ? 42 : 52,
                    "uppercase".equalsIgnoreCase(templateProfile.getHeadingStyle()) ? 13.5f : 13f,
                    dense ? 10.5f : 11f,
                    compact ? 14f : 16f
            );
        }
        if ("minimal".equalsIgnoreCase(style)) {
            return new LayoutConfig(48, 13f, 11f, 18f);
        }
        return new LayoutConfig(48, 13f, 11f, 15f);
    }

    private record LayoutConfig(float margin, float headingFontSize, float bodyFontSize, float leading) {
    }
}
