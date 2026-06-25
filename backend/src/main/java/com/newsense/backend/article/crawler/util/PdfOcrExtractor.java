package com.newsense.backend.article.crawler.util;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class PdfOcrExtractor {

    private static final int OCR_DPI = 250;

    private final CrawlerProperties properties;

    public String extractText(String pdfUrl, String primaryText) {
        if (hasSufficientText(primaryText)) {
            return primaryText.trim();
        }
        if (properties.tessDataPath() == null || properties.tessDataPath().isBlank()) {
            return primaryText == null ? "" : primaryText.trim();
        }
        try {
            byte[] bytes = URI.create(pdfUrl).toURL().openStream().readAllBytes();
            return extractText(bytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read PDF for OCR: " + pdfUrl, exception);
        }
    }

    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            Tesseract tesseract = createTesseract();
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder builder = new StringBuilder();
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, OCR_DPI, ImageType.RGB);
                builder.append(tesseract.doOCR(image)).append('\n');
            }
            return builder.toString().trim();
        } catch (IOException | TesseractException exception) {
            throw new IllegalStateException("Failed to OCR PDF", exception);
        }
    }

    private boolean hasSufficientText(String text) {
        return text != null && text.replaceAll("\\s+", "").length() >= properties.pdfMinTextLength();
    }

    private Tesseract createTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("kor");
        tesseract.setDatapath(properties.tessDataPath());
        return tesseract;
    }
}
