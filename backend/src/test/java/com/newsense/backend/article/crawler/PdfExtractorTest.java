package com.newsense.backend.article.crawler;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.util.PdfOcrExtractor;
import com.newsense.backend.article.crawler.util.PdfTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PDF extractor unit tests")
class PdfExtractorTest {

    @Test
    void pdfTextExtractor_extractsTextFromPdfBytes() throws Exception {
        PdfTextExtractor extractor = new PdfTextExtractor();

        String text = extractor.extractText(createPdf("Newsense economic article"));

        assertThat(text).contains("Newsense economic article");
    }

    @Test
    void pdfOcrExtractor_returnsPrimaryTextWhenItIsSufficient() {
        CrawlerProperties properties = new CrawlerProperties(
                true,
                false,
                0,
                0,
                0,
                1000,
                1,
                1,
                0,
                0,
                800,
                5,
                "",
                "",
                "",
                "",
                null,
                List.of()
        );
        PdfOcrExtractor extractor = new PdfOcrExtractor(properties);

        String text = extractor.extractText("https://example.com/sample.pdf", "충분한 1차 텍스트입니다.");

        assertThat(text).isEqualTo("충분한 1차 텍스트입니다.");
    }

    private byte[] createPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(72, 720);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
