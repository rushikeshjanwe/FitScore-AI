package com.rushikesh.fitscore.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PdfTextExtractor {

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is missing or empty");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF resumes are supported");
        }
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document).trim();
            if (text.isBlank()) {
                throw new IllegalArgumentException(
                        "No text found in the PDF. Scanned/image-only resumes are not supported");
            }
            return text;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the PDF file", e);
        }
    }
}
