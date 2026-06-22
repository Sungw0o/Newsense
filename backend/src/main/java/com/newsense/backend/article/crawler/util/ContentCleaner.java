package com.newsense.backend.article.crawler.util;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class ContentCleaner {

    public String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        return Jsoup.parse(rawText)
                .text()
                .replace('\u00A0', ' ')
                .replace("\u200B", "")
                .replaceAll("[\\t\\x0B\\f\\r ]+", " ")
                .replaceAll(" *\\n *", "\n")
                .trim();
    }
}
