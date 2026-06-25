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
            assertThat(response.results().getFirst().scoreBreakdown()).isNotNull();
            assertThat(response.results().getFirst().scoreBreakdown().keywordScore()).isPositive();
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

            RagSearchResponse response = ragRetrievalService.search("기준금리", 10);

            assertThat(response.results()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // search() — 하이브리드 모드 (vector enabled)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("search — hybrid (vector enabled)")
    class HybridSearch {

        @Test
        @DisplayName("벡터 검색 결과 + 키워드 하이브리드 점수 반환")
        void search_hybridCombinesVectorAndKeyword() {
            given(vectorSearchProperties.enabled()).willReturn(true);
            ArticleMeta article = TestFixtures.article(1L);
            ArticleContent content = TestFixtures.content("mongo-1", "기준금리 인하 기사");
            given(embeddingClient.embed(anyString())).willReturn(List.of(0.1, 0.2, 0.3));
            given(vectorSearchService.search(anyList(), anyInt()))
                    .willReturn(List.of(new VectorResult("mongo-1", 0.9)));
            given(articleMetaRepository.findAllByMongoDocumentIdIn(List.of("mongo-1")))
                    .willReturn(List.of(article));
            given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

            RagSearchResponse response = ragRetrievalService.search("기준금리", 5);

            assertThat(response.results()).hasSize(1);
            assertThat(response.results().getFirst().score()).isPositive();
            assertThat(response.results().getFirst().scoreBreakdown().vectorScore()).isPositive();
            then(vectorSearchService).should().search(anyList(), anyInt());
        }

        @Test
        @DisplayName("임베딩 빈 리스트 → 키워드 검색으로 fallback")
        void search_fallsBackWhenEmbeddingEmpty() {
            given(vectorSearchProperties.enabled()).willReturn(true);
            ArticleMeta article = TestFixtures.article(1L);
            ArticleContent content = TestFixtures.content("mongo-1", "기준금리 기사");
            given(embeddingClient.embed(anyString())).willReturn(List.of());
            given(articleMetaRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(article)));
            given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

            RagSearchResponse response = ragRetrievalService.search("기준금리", 5);

            then(vectorSearchService).shouldHaveNoInteractions();
            assertThat(response.results()).isNotNull();
        }

        @Test
        @DisplayName("벡터 검색 결과 없음 → 키워드 검색으로 fallback")
        void search_fallsBackWhenVectorEmpty() {
            given(vectorSearchProperties.enabled()).willReturn(true);
            ArticleMeta article = TestFixtures.article(1L);
            ArticleContent content = TestFixtures.content("mongo-1", "기준금리 기사");
            given(embeddingClient.embed(anyString())).willReturn(List.of(0.1, 0.2));
            given(vectorSearchService.search(anyList(), anyInt())).willReturn(List.of());
            given(articleMetaRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(article)));
            given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

            RagSearchResponse response = ragRetrievalService.search("기준금리", 5);

            assertThat(response.results()).isNotNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // recommendForWeakness()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("recommendForWeakness")
    class Recommend {

        @Test
        @DisplayName("오답노트 없음 → 빈 응답")
        void recommendForWeakness_returnsEmptyWhenNoWrongNotes() {
            given(wrongNoteRepository.findWeaknessSignals(7L)).willReturn(List.of());

            RagRecommendationResponse response = ragRetrievalService.recommendForWeakness(7L, 5);

            assertThat(response.weaknessTerms()).isEmpty();
            assertThat(response.recommendations()).isEmpty();
        }

        @Test
        @DisplayName("오답노트 용어로 기사 검색")
        void recommendForWeakness_searchesArticlesByWrongNoteTerms() {
            given(vectorSearchProperties.enabled()).willReturn(false);
            ArticleMeta article = TestFixtures.article(1L);
            ArticleContent content = TestFixtures.content("mongo-1", "환율 변동과 금리 정책 기사");
            WrongNote note = WrongNote.create(
                    TestFixtures.user(7L),
                    TestFixtures.quiz(10L, article, "O"),
                    "X",
                    List.of("환율", "금리")
            );
            given(wrongNoteRepository.findWeaknessSignals(7L)).willReturn(List.of(note));
            given(articleMetaRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(article)));
            given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

            RagRecommendationResponse response = ragRetrievalService.recommendForWeakness(7L, 0);

            assertThat(response.weaknessTerms()).containsExactly("환율", "금리");
            assertThat(response.recommendations()).hasSize(1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // retrieveQuizEvidence()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("키워드 매칭 청크 반환, 빈 키워드 시 fallback")
    void retrieveQuizEvidence_usesChunksWhenKeywordsMatchOrFallsBack() {
        ArticleContent content = TestFixtures.content("mongo-1", "기준금리 설명 문장");

        List<String> matched = ragRetrievalService.retrieveQuizEvidence(
                "기준금리",
                content,
                List.of(new EconomicTermContext("금리", "이자율"))
        );
        List<String> fallback = ragRetrievalService.retrieveQuizEvidence("", content, List.of());

        assertThat(matched).contains("기준금리 설명 문장");
        assertThat(fallback).containsExactly("기준금리 설명 문장");
    }
}
