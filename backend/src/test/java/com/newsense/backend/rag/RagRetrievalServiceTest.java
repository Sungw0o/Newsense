package com.newsense.backend.rag;

import com.newsense.backend.ai.embedding.OpenAiEmbeddingClient;
import com.newsense.backend.ai.quiz.EconomicTermContext;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.rag.config.VectorSearchProperties;
import com.newsense.backend.rag.dto.RagRecommendationResponse;
import com.newsense.backend.rag.dto.RagSearchResponse;
import com.newsense.backend.rag.service.RagRetrievalService;
import com.newsense.backend.rag.service.RagVectorSearchService;
import com.newsense.backend.rag.service.RagVectorSearchService.VectorResult;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.wrongnote.domain.WrongNote;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagRetrievalService unit tests")
class RagRetrievalServiceTest {

    @InjectMocks
    RagRetrievalService ragRetrievalService;

    @Mock ArticleMetaRepository articleMetaRepository;
    @Mock ArticleContentRepository articleContentRepository;
    @Mock WrongNoteRepository wrongNoteRepository;
    @Mock OpenAiEmbeddingClient embeddingClient;
    @Mock RagVectorSearchService vectorSearchService;
    @Mock VectorSearchProperties vectorSearchProperties;

    // ─────────────────────────────────────────────────────────────────────────
    // search() — 키워드 전용 모드 (vector disabled)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("search — keyword-only (vector disabled)")
    class KeywordSearch {

        @Test
        @DisplayName("키워드 매칭 기사 반환 및 score 양수")
        void search_returnsRankedArticleMatches() {
            given(vectorSearchProperties.enabled()).willReturn(false);
            ArticleMeta article = TestFixtures.article(1L);
            ArticleContent content = TestFixtures.content("mongo-1", "기준금리 인하가 금융 시장에 영향을 준다.");
            given(articleMetaRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(article)));
            given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

            RagSearchResponse response = ragRetrievalService.search("기준금리 금융", 10);

            assertThat(response.keywords()).contains("기준금리", "금융");
            assertThat(response.results()).hasSize(1);
            assertThat(response.results().getFirst().score()).isPositive();
        }

        @Test
        @DisplayName("빈 쿼리 → CustomException")
        void search_rejectsBlankQuery() {
            assertThatThrownBy(() -> ragRetrievalService.search(" ", 5))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("매칭 없는 기사는 결과에서 제외")
        void search_excludesZeroScoreArticles() {
            given(vectorSearchProperties.enabled()).willReturn(false);
            ArticleMeta article = TestFixtures.article(1L);
            ArticleContent content = TestFixtures.content("mongo-1", "아무 관련 없는 내용");
            given(articleMetaRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(article)));
            given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

         