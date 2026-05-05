package com.resumefit.service;

import com.resumefit.dto.ResumeParseResponse;
import com.resumefit.exception.BadRequestException;
import com.resumefit.util.ResumeSectionParser;
import com.resumefit.util.ResumeTextCleaner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeParsingService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final ResumeTemplateProfileService templateProfileService;

    public ResumeParsingService(ResumeTemplateProfileService templateProfileService) {
        this.templateProfileService = templateProfileService;
    }

    public ResumeParseResponse parseResume(MultipartFile file) {
        validateFile(file);

        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (fileName.endsWith(".docx") || DOCX_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
            return parseDocx(file);
        }
        return parsePdf(file);
    }

    private ResumeParseResponse parsePdf(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new BadRequestException("Encrypted PDFs are not supported. Please upload an unlocked resume PDF.");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String extractedText = stripper.getText(document);
            String cleanText = ResumeTextCleaner.clean(extractedText);
            List<String> warnings = buildWarnings(extractedText, cleanText);

            if (cleanText.isBlank()) {
                throw new BadRequestException("No readable text found in this PDF. Please upload a text-based PDF, not a scanned image.");
            }

            List<com.resumefit.dto.ResumeSectionDto> sections = ResumeSectionParser.parse(cleanText);
            return ResumeParseResponse.builder()
                    .fileName(originalFilename)
                    .fileType("pdf")
                    .uploadMode("PDF Mode")
                    .pageCount(document.getNumberOfPages())
                    .characterCount(cleanText.length())
                    .cleanText(cleanText)
                    .sections(sections)
                    .templateProfile(templateProfileService.buildProfile(
                            cleanText,
                            sections,
                            document.getNumberOfPages(),
                            "pdf"
                    ))
                    .warnings(warnings)
                    .build();
        } catch (BadRequestException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BadRequestException("Unable to read this PDF. It may be corrupted or unsupported.");
        }
    }

    private ResumeParseResponse parseDocx(MultipartFile file) {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            List<String> rawLines = new ArrayList<>();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    rawLines.add(text.trim());
                }
            }

            String cleanText = ResumeTextCleaner.clean(String.join("\n", rawLines));
            if (cleanText.isBlank()) {
                throw new BadRequestException("No readable text found in this DOCX file.");
            }

            List<com.resumefit.dto.ResumeSectionDto> sections = ResumeSectionParser.parse(cleanText);
            return ResumeParseResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType("docx")
                    .uploadMode("DOCX Preserve Mode")
                    .pageCount(1)
                    .characterCount(cleanText.length())
                    .cleanText(cleanText)
                    .sections(sections)
                    .templateProfile(templateProfileService.buildProfile(
                            cleanText,
                            sections,
                            1,
                            "docx"
                    ))
                    .warnings(List.of("DOCX preserve mode works best when headings and sections are clearly structured."))
                    .build();
        } catch (Exception exception) {
            throw new BadRequestException("Unable to read this DOCX file. It may be corrupted or unsupported.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Resume file is required.");
        }

        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType();

        boolean isPdf = fileName.endsWith(".pdf") || PDF_CONTENT_TYPE.equalsIgnoreCase(contentType);
        boolean isDocx = fileName.endsWith(".docx") || DOCX_CONTENT_TYPE.equalsIgnoreCase(contentType);

        if (!isPdf && !isDocx) {
            throw new BadRequestException("Only PDF and DOCX resume files are supported.");
        }
    }

    private List<String> buildWarnings(String rawText, String cleanText) {
        List<String> warnings = new ArrayList<>();

        if (rawText == null || rawText.isBlank()) {
            warnings.add("PDFBox did not find raw text. This may be a scanned resume.");
        }

        if (cleanText.length() < 300) {
            warnings.add("Extracted text is short. Check whether the PDF has images, columns, or hidden text.");
        }

        return warnings;
    }
}
