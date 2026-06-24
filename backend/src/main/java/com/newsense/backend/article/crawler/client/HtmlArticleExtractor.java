package com.newsense.backend.article.crawler.client;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class HtmlArticleExtractor {

    private static final String USER_AGENT = "NewsenseCrawler/1.0";
    private static final String BODY_SELECTORS = String.join(", ",
            "article",
            "div#dic_area",
            "div#newsct_article",
            "div.article_body",
            "div.article-view-content-div",
            "div#articeBody",
            "div#articleBody",
            "div.article-content",
            "div.news_end",
            "main"
    );

    private final CrawlerProperties properties;

    public Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(properties.connectionTimeout())
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .get();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch article HTML: " + url, exception);
        }
    }

    public String extractTitle(Document document) {
        Element title = document.selectFirst("h2#title_area, h1, meta[property=og:title], title");
        if (title == null) {
            return "";
        }
        String text = title.hasAttr("content") ? title.attr("content") : title.text();
        return text == null ? "" : text.trim();
    }

    public String extractBody(Document document) {
        Element body = document.selectFirst(BODY_SELECTORS);
        if (body != null && !body.text().isBlank()) {
            return body.text().trim();
        }
        Element description = document.selectFirst("meta[property=og:description], meta[name=description]");
        return description == null ? "" : description.attr("content").trim();
    }
}
