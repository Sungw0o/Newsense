package com.newsense.backend.rag;

import com.newsense.backend.ai.embedding.OpenAiEmbeddingClient;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.rag.config.VectorSearchProperties;
import com.newsense.backend.rag.service.ArticleEmbeddingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleEmbeddingService unit tests")
class ArticleEmbeddingServiceTest {

    @InjectMocks
    ArticleEmbeddingService articleEmbeddingService;

    @Mock OpenAiEmbeddingClient embeddingClient;
    @Mock MongoTemplate mongoTemplate;
    @Mock VectorSearchProperties vectorSearchProperties;

    @Test
    @DisplayName("enabled=false 이면 임베딩 호출 없음")
    void generateAndStoreAsync_skipsWhenDisabled() {
        given(vectorSearchProperties.enabled()).willReturn(false);

        articleEmbeddingService.generateAndStoreAsync("id-1", "제목", "본문");

        then(embeddingClient).shouldHaveNoInteractions();
        then(mongoTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("임베딩 성공 시 MongoDB updateFirst 호출")
    void generateAndStoreAsync_storesEmbeddingWhenSuccessful() {
        given(vectorSearchProperties.enabled()).willReturn(true);
        given(embeddingClient.embed(anyString())).willReturn(List.of(0.1, 0.2, 0.3));

        articleEmbeddingService.generateAndStoreAsync("id-1", "기사 제목", "기사 본문 내용");

        then(mongoTemplate).should().updateFirst(
                any(Query.class),
                any(Update.class),
                eq(ArticleContent.class)
        );
    }

    @Test
    @DisplayName("임베딩 결과 빈 리스트 → MongoDB 호출 없음")
    void generateAndStoreAsync_skipsStoreWhenEmbeddingEmpty() {
        given(vectorSearchProperties.enabled()).willReturn(true);
        given(embeddingClient.embed(anyString())).willReturn(List.of());

        articleEmbeddingService.generateAndStoreAsync("id-1", "제목", "본문");

        then(mongoTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("임베딩 API 예외 → 조용히 처리, MongoDB 호출 없음")
    void generateAndStoreAsync_handlesExceptionGracefully() {
        given(vectorSearchProperties.enabled()).willReturn(true);
        given(embeddingClient.embed(anyString())).willThrow(new RuntimeException("API 오류"));

        // 예외가 외부로 전파되지 않아야 함
        articleEmbeddingService.generateAndStoreAsync("id-1", "제목", "본문");

        then(mongoTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("임베딩 입력에 제목과 본문이 포함됨")
    void generateAndStoreAsync_includesTitleInEmbeddingInput() {
        given(vectorSearchProperties.enabled()).willReturn(true);
        given(embeddingClient.embed(anyString())).willReturn(List.of(0.1, 0.2));

        ArgumentCaptor<String> inputCaptor = ArgumentCaptor.forClass(String.class);

        articleEmbeddingService.generateAndStoreAsync("id-1", "기준금리 제목", "본문 내용");

        then(embeddingClient).should().embed(inputCaptor.capture());
        assertThat(inputCaptor.getValue()).contains("기준금리 제목", "본문 내용");
    }
}
