package com.resumefit.service;

import com.resumefit.dto.ResumeExportRequest;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

@Service
public class ResumeExportService {

    public byte[] exportDocx(ResumeExportRequest request) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            List<String> lines = List.of(request.getResumeText().split("\\n"));

            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index);
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setSpacingAfter(90);
                XWPFRun run = paragraph.createRun();
                String safeLine = line == null ? "" : line.trim();
                run.setText(safeLine);
                run.setFontFamily("Calibri");

                boolean heading = safeLine.equals(safeLine.toUpperCase()) && safeLine.length() < 40 && !safeLine.isBlank();
                boolean titleLine = index == 0 && !safeLine.isBlank();
                boolean contactLine = index == 1 && safeLine.contains("|");

                if (titleLine) {
                    run.setBold(true);
                    run.setFontSize(16);
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                    paragraph.setSpacingAfter(60);
                } else if (contactLine) {
                    run.setFontSize(9);
                    paragraph.setAlignment(ParagraphAlignment.CENTER);
                    paragraph.setSpacingAfter(140);
                } else if (heading) {
                    run.setBold(true);
                    run.setFontSize(12);
                    paragraph.setSpacingBefore(140);
                    paragraph.setSpacingAfter(60);
                } else if (safeLine.startsWith("-")) {
                    run.setFontSize(10);
                    paragraph.setIndentationLeft(260);
                    paragraph.setFirstLineIndent(-120);
                    paragraph.setSpacingAfter(40);
                } else {
                    run.setFontSize(10);
                }

                if (safeLine.isBlank()) {
                    paragraph.setSpacingAfter(40);
                    paragraph.setAlignment(ParagraphAlignment.LEFT);
                }
            }

            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to export DOCX resume.", exception);
        }
    }
}
