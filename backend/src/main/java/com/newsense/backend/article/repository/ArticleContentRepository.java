package com.newsense.backend.article.repository;

import com.newsense.backend.article.document.ArticleContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleContentRepository extends MongoRepository<ArticleContent, String> {

    Optional<ArticleContent> findBySourceUrl(String sourceUrl);

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsByContentHash(String contentHash);

    /** 임베딩이 존재하는 문서만 조회 (로컬 코사인 유사도 검색용) */
    @Query("{ 'embedding': { '$exists': true, '$ne': null, '$not': { '$size': 0 } } }")
    List<ArticleContent> findAllWithEmbedding();

    /** 임베딩이 없는 문서 ID 목록 조회 (백필용) */
    @Query(value = "{ '$or': [ { 'embedding': { '$exists': false } }, { 'embedding': null } ] }", fields = "{ '_id': 1, 'title': 1, 'cleanText': 1 }")
    List<ArticleContent> findAllWithoutEmbedding();
}
