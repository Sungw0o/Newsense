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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleFeedService unit tests")
class ArticleFeedServiceTest {

    private static final List<String> EXCLUDED_TITLE_KEYWORDS = List.of(
            "인사",
            "공고",
            "공지",
            "채용",
            "행사",
            "일정",
            "안내",
            "동정",
            "입찰",
            "모집"
    );

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
        verify(articleMetaRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getArticles_buildsSpecificationWithExcludedTitleKeywords() {
        ArticleMeta article = TestFixtures.article(1L);
        given(articleMetaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(article)));

        articleFeedService.getArticles(null, null, 0, 10, ArticleFeedSort.LATEST);

        ArgumentCaptor<Specification<ArticleMeta>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(articleMetaRepository).findAll(specificationCaptor.capture(), any(Pageable.class));

        Root<ArticleMeta> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate basePredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);
        Predicate notLikePredicate = mock(Predicate.class);
        Path<String> titlePath = mock(Path.class);
        Expression<String> lowerTitle = mock(Expression.class);

        given(criteriaBuilder.conjunction()).willReturn(basePredicate);
        given(root.get("title")).willReturn((Path) titlePath);
        given(criteriaBuilder.lower(titlePath)).willReturn(lowerTitle);
        given(criteriaBuilder.notLike(any(Expression.class), anyString())).willReturn(notLikePredicate);
        given(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).willReturn(combinedPredicate);

        Predicate result = specificationCaptor.getValue().toPredicate(root, query, criteriaBuilder);

        assertThat(result).isEqualTo(combinedPredicate);
        verify(criteriaBuilder, times(EXCLUDED_TITLE_KEYWORDS.size()))
                .notLike(any(Expression.class), anyString());
        for (String keyword : EXCLUDED_TITLE_KEYWORDS) {
            verify(criteriaBuilder).notLike(lowerTitle, "%" + keyword + "%");
        }
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
