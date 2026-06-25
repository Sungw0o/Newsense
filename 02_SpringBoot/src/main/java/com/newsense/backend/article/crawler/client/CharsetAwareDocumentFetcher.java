package com.newsense.backend.article.crawler.client;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CharsetAwareDocumentFetcher {

    private static final String USER_AGENT = "NewsenseCrawler/1.0";
    private static final List<String> FALLBACK_CHARSETS = List.of("UTF-8", "MS949", "EUC-KR");

    public Document fetch(String url, int timeout, boolean ignoreContentType) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(timeout)
                .followRedirects(true)
                .ignoreContentType(ignoreContentType)
                .ignoreHttpErrors(false)
                .execute();
        return parseBest(response, url);
    }

    private Document parseBest(Connection.Response response, String url) {
        byte[] bytes = response.bodyAsBytes();
        List<Document> documents = candidateCharsets(response.charset()).stream()
                .filter(Charset::isSupported)
                .map(Charset::forName)
                .map(charset -> {
                    try {
                        return Jsoup.parse(new ByteArrayInputStream(bytes), charset.name(), url);
                    } catch (IOException exception) {
                        throw new IllegalStateException("Failed to parse crawler response", exception);
                    }
                })
                .toList();
        if (documents.isEmpty()) {
            return Jsoup.parse(response.body(), url);
        }
        return documents.stream()
                .max((left, right) -> Integer.compare(score(left.text()), score(right.text())))
                .orElseGet(() -> Jsoup.parse(response.body(), url));
    }

    private List<String> candidateCharsets(String responseCharset) {
        Set<String> charsets = new LinkedHashSet<>();
        if (responseCharset != null && !responseCharset.isBlank()) {
            charsets.add(responseCharset);
        }
        charsets.addAll(FALLBACK_CHARSETS);
        return new ArrayList<>(charsets);
    }

    private int score(String text) {
        int korean = 0;
        int replacement = 0;
        int mojibake = 0;

        for (int index = 0; index < text.length(); index++) {
            char value = text.charAt(index);
            if (value >= '가' && value <= '힣') {
                korean++;
            } else if (value == '\uFFFD') {
                replacement++;
            } else if (value == 'Ã' || value == 'Â' || value == '¤' || value == '½' || value == 'Å') {
                mojibake++;
            }
        }
        return korean * 5 - replacement * 30 - mojibake * 10;
    }
}
