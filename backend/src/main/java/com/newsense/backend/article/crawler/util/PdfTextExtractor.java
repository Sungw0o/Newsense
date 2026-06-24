package com.newsense.backend.article.crawler.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
public class PdfTextExtractor {

    public String extractText(String pdfUrl) {
        try {
            byte[] bytes = URI.create(pdfUrl).toURL().openStream().readAllBytes();
            return extractText(bytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to extract PDF text: " + pdfUrl, exception);
        }
    }

    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(document).trim();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to extract PDF text from bytes", exception);
        }
    }
}
