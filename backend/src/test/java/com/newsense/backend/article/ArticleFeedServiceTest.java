package com.newsense.backend.article;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.dto.ArticleCategoryResponse;
import com.newsense.backend.article.dto.ArticleFeedPageResponse;
import com.newsense.backend.article.dto.ArticleFeedSort;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.service.ArticleFeedService;
import com.newsense.backend.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleFeedService unit tests")
class ArticleFeedServiceTest {

    @InjectMocks
    ArticleFeedService articleFeedService;

    @Mock
    ArticleMetaRepository articleMetaRepository;

    @Test
    void getArticles_mapsPageResponseAndUsesSort() {
        ArticleMeta article = TestFixtures.article(1L);
        given(articleMetaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(article)));

        ArticleFeedPageResponse response = articleFeedService.getArticles(
                ArticleCategory.FINANCE_INVESTMENT,
                ArticleDifficulty.BASIC,
                0,
                10,
                ArticleFeedSort.LATEST
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(articleMetaRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getCategories_returnsAllCategoriesWithZeroDefault() {
        ArticleMetaRepository.CategoryCount count = mock(ArticleMetaRepository.CategoryCount.class);
        given(count.getCategory()).willReturn(ArticleCategory.FINANCE_INVESTMENT);
        given(count.getArticleCount()).willReturn(3L);
        given(articleMetaRepository.countArticlesByCategory()).willReturn(List.of(count));

        List<ArticleCategoryResponse> responses = articleFeedService.getCategories();

        assertThat(responses).hasSize(ArticleCategory.values().length);
        assertThat(responses)
                .filteredOn(response -> response.categoryId() == ArticleCategory.FINANCE_INVESTMENT)
                .singleElement()
                .extracting(ArticleCategoryResponse::articleCount)
                .isEqualTo(3L);
        assertThat(responses)
                .filteredOn(response -> response.categoryId() == ArticleCategory.MACRO_ECONOMY)
                .singleElement()
                .extracting(ArticleCategoryResponse::articleCount)
                .isEqualTo(0L);
    }
}
