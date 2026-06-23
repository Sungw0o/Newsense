package com.newsense.backend.article.service;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.article.dto.ArticleCategoryResponse;
import com.newsense.backend.article.dto.ArticleFeedPageResponse;
import com.newsense.backend.article.dto.ArticleFeedSort;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFeedService {

    private final ArticleMetaRepository articleMetaRepository;

    public ArticleFeedPageResponse getArticles(
            ArticleCategory category,
            ArticleDifficulty difficulty,
            int page,
            int size,
            ArticleFeedSort sort
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, sort.toSort());
        Page<ArticleCardResponse> articles = articleMetaRepository
                .findAll(createSpecification(category, difficulty), pageRequest)
                .map(ArticleCardResponse::from);
        return ArticleFeedPageResponse.from(articles);
    }

    public List<ArticleCategoryResponse> getCategories() {
        Map<ArticleCategory, Long> counts = new EnumMap<>(ArticleCategory.class);
        articleMetaRepository.countArticlesByCategory()
                .forEach(result -> counts.put(result.getCategory(), result.getArticleCount()));

        return Arrays.stream(ArticleCategory.values())
                .map(category -> new ArticleCategoryResponse(
                        category,
                        category.getDisplayName(),
                        counts.getOrDefault(category, 0L)
                ))
                .toList();
    }

    private Specification<ArticleMeta> createSpecification(
            ArticleCategory category,
            ArticleDifficulty difficulty
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (category != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }
            return predicate;
        };
    }
}
