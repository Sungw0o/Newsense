package com.newsense.backend.article.repository;

import com.newsense.backend.article.document.ArticleContent;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleContentRepository extends MongoRepository<ArticleContent, String> {

    Optional<ArticleContent> findBySourceUrl(String sourceUrl);

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsByContentHash(String contentHash);
}
