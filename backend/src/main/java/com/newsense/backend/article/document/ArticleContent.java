package com.newsense.backend.article.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Document(collection = "article_content")
public class ArticleContent {

    @Id
    private String id;

    private String sourceKey;
    private String source;

    @Indexed(unique = true)
    private String sourceUrl;

    private String title;
    private LocalDate publishedAt;
    private String rawText;
    private String cleanText;
    private List<String> chunks;

    @Indexed(unique = true)
    private String contentHash;

    private LocalDateTime collectedAt;

    public static ArticleContent create(
            String sourceKey,
            String source,
            String sourceUrl,
            String title,
            LocalDate publishedAt,
            String rawText,
            String cleanText,
            List<String> chunks,
            String contentHash
    ) {
        ArticleContent content = new ArticleContent();
        content.sourceKey = sourceKey;
        content.source = source;
        content.sourceUrl = sourceUrl;
        content.title = title;
        content.publishedAt = publishedAt;
        content.rawText = rawText;
        content.cleanText = cleanText;
        content.chunks = List.copyOf(chunks);
        content.contentHash = contentHash;
        content.collectedAt = LocalDateTime.now();
        return content;
    }
}
