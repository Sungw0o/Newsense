package com.newsense.backend.article.repository;

import com.newsense.backend.article.document.ArticleContent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleContentRepository extends MongoRepository<ArticleContent, String> {

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsByContentHash(String contentHash);
}
