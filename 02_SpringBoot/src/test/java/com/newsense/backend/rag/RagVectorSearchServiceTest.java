package com.newsense.backend.rag;

import com.newsense.backend.rag.config.VectorSearchProperties;
import com.newsense.backend.rag.service.RagVectorSearchService;
import com.newsense.backend.rag.service.RagVectorSearchService.VectorResult;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.article.document.ArticleContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagVectorSearchService unit tests")
class RagVectorSearchServiceTest {

    @InjectMocks
    RagVectorSearchService ragVectorSearchService;

    @Mock VectorSearchProperties props;
    @Mock MongoTemplate mongoTemplate;

    @Nested
    @DisplayName("local cosine similarity 검색")
    class LocalMode {

        @Test
        @DisplayName("쿼리 임베딩 null → 빈 결과")
        void search_returnsEmptyWhenQueryNull() {
            List<VectorResult> results = ragVectorSearchService.search(null, 5);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("쿼리 임베딩 빈 리스트 → 빈 결과")
        void search_returnsEmptyWhenQueryEmpty() {
            List<VectorResult> results = ragVectorSearchService.search(List.of(), 5);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("로컬 모드: 임베딩 있는 기사 없음 → 빈 결과")
        void search_localMode_noArticlesWithEmbedding() {
            given(props.isAtlasMode()).willReturn(false);
            given(mongoTemplate.find(any(Query.class), eq(ArticleContent.class))).willReturn(List.of());

            List<VectorResult> results = ragVectorSearchService.search(List.of(0.1, 0.2, 0.3), 5);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("로컬 모드: 코사인 유사도 계산 후 상위 limit 반환")
        void search_localMode_returnsTopByCosineSimilarity() {
            given(props.isAtlasMode()).willReturn(false);

            ArticleContent highSim = TestFixtures.content("id-1", "기사1");
            ArticleContent lowSim = TestFixtures.content("id-2", "기사2");
            ArticleContent noEmbed = TestFixtures.content("id-3", "기사3");

            // id-1: 쿼리와 동일 방향 (코사인 = 1.0)
            ReflectionTestUtils.setField(highSim, "embedding", List.of(1.0, 0.0, 0.0));
            // id-2: 직교 (코사인 = 0.0 → 필터됨)
            ReflectionTestUtils.setField(lowSim, "embedding", List.of(0.0, 1.0, 0.0));
            // id-3: 임베딩 없음
            ReflectionTestUtils.setField(noEmbed, "embedding", null);

            given(mongoTemplate.find(any(Query.class), eq(ArticleContent.class)))
                    .willReturn(List.of(highSim, lowSim));

            // 쿼리: [1, 0, 0]
            List<VectorResult> results = ragVectorSearchService.search(List.of(1.0, 0.0, 0.0), 5);

            // lowSim(score=0.0)은 필터되고 highSim만 반환
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().contentId()).isEqualTo("id-1");
            assertThat(results.getFirst().score()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("로컬 모드: limit 초과 결과 → 상위 limit만 반환")
        void search_localMode_respectsLimit() {
            given(props.isAtlasMode()).willReturn(false);

            List<ArticleContent> articles = List.of(
                    articleWithEmbedding("a1", List.of(1.0, 0.0)),
                    articleWithEmbedding("a2", List.of(0.9, 0.1)),
                    articleWithEmbedding("a3", List.of(0.8, 0.2))
            );
            given(mongoTemplate.find(any(Query.class), eq(ArticleContent.class))).willReturn(articles);

            List<VectorResult> results = ragVectorSearchService.search(List.of(1.0, 0.0), 2);

            assertThat(results).hasSize(2);
            assertThat(results.get(0).score()).isGreaterThanOrEqualTo(results.get(1).score());
        }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private ArticleContent articleWithEmbedding(String id, List<Double> embedding) {
        ArticleContent content = TestFixtures.content(id, "기사 본문");
        ReflectionTestUtils.setField(content, "embedding", embedding);
        return content;
    }
}
