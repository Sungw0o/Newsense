package com.newsense.backend.rag;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.rag.config.VectorSearchProperties;
import com.newsense.backend.rag.runner.EmbeddingBackfillRunner;
import com.newsense.backend.rag.service.ArticleEmbeddingService;
import com.newsense.backend.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmbeddingBackfillRunner unit tests")
class EmbeddingBackfillRunnerTest {

    @InjectMocks
    EmbeddingBackfillRunner runner;

    @Mock ArticleContentRepository articleContentRepository;
    @Mock ArticleEmbeddingService articleEmbeddingService;
    @Mock VectorSearchProperties vectorSearchProperties;

    private final DefaultApplicationArguments emptyArgs =
            new DefaultApplicationArguments(new String[0]);

    @Test
    void run_vectorSearchDisabled_skipsBackfill() throws Exception {
        given(vectorSearchProperties.enabled()).willReturn(false);

        runner.run(emptyArgs);

        verify(articleContentRepository, never()).findAllWithoutEmbedding();
        verify(articleEmbeddingService, never())
                .generateAndStoreAsync(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void run_noMissingEmbeddings_skipsGeneration() throws Exception {
        given(vectorSearchProperties.enabled()).willReturn(true);
        given(articleContentRepository.findAllWithoutEmbedding()).willReturn(List.of());

        runner.run(emptyArgs);

        verify(articleEmbeddingService, never())
                .generateAndStoreAsync(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void run_articleWithCleanText_submitsEmbeddingAsync() throws Exception {
        given(vectorSearchProperties.enabled()).willReturn(true);
        ArticleContent content = TestFixtures.content("c1", "기준금리 인하에 따른 영향");
        given(articleContentRepository.findAllWithoutEmbedding()).willReturn(List.of(content));

        runner.run(emptyArgs);

        verify(articleEmbeddingService).generateAndStoreAsync("c1", "금융 기사", "기준금리 인하에 따른 영향");
    }

    @Test
    void run_articleWithBlankCleanText_skipsArticle() throws Exception {
        given(vectorSearchProperties.enabled()).willReturn(true);
        ArticleContent content = TestFixtures.content("c2", "   ");
        given(articleContentRepository.findAllWithoutEmbedding()).willReturn(List.of(content));

        runner.run(emptyArgs);

        verify(articleEmbeddingService, never())
                .generateAndStoreAsync(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }
}
