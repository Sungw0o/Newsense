package com.newsense.backend.article.service;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.repository.ArticleReadRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 개인화 기사 추천 서비스.
 *
 * <p>추천 전략:
 * <ol>
 *   <li>사용자 관심 카테고리 기반 미독(未讀) 기사 (최우선)</li>
 *   <li>오답노트 취약 카테고리 기반 미독 기사 (보완)</li>
 *   <li>전체 미독 최신 기사 (부족분 채우기)</li>
 * </ol>
 * 이미 읽은 기사는 제외합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleRecommendationService {

    private static final int DEFAULT_LIMIT     = 10;
    private static final int MAX_LIMIT         = 20;
    private static final int CANDIDATE_POOL    = 60;

    private final ArticleMetaRepository  articleMetaRepository;
    private final ArticleReadRepository  articleReadRepository;
    private final WrongNoteRepository    wrongNoteRepository;
    private final UserRepository         userRepository;

    @Transactional(readOnly = true)
    public List<ArticleCardResponse> recommend(Long userId, int limit) {
        int effectiveLimit = Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT);

        // 이미 읽은 기사 ID 집합
        Set<Long> readIds = articleReadRepository.findAllByUserId(userId)
                .stream()
                .map(r -> r.getArticle().getId())
                .collect(Collectors.toSet());

        // 사용자 관심 카테고리
        List<ArticleCategory> interestCategories = userRepository.findById(userId)
                .map(User::getInterests)
                .orElse(List.of())
                .stream()
                .map(this::toCategory)
                .filter(c -> c != null)
                .toList();

        // 취약 카테고리 (오답노트 기반)
        List<ArticleCategory> weakCategories = wrongNoteRepository.findWeaknessSignals(userId)
                .stream()
                .map(note -> note.getCategory())
                .filter(c -> c != null)
                .distinct()
                .limit(3)
                .toList();

        Set<ArticleCardResponse> result = new LinkedHashSet<>();

        // 1. 관심 카테고리 미독 기사
        if (!interestCategories.isEmpty()) {
            fetchUnread(interestCategories, readIds, effectiveLimit).forEach(result::add);
        }

        // 2. 취약 카테고리 미독 기사
        if (result.size() < effectiveLimit && !weakCategories.isEmpty()) {
            List<ArticleCategory> combined = new ArrayList<>(weakCategories);
            combined.removeAll(interestCategories);
            if (!combined.isEmpty()) {
                fetchUnread(combined, readIds, effectiveLimit - result.size()).forEach(result::add);
            }
        }

        // 3. 부족분: 전체 최신 미독 기사
        if (result.size() < effectiveLimit) {
            Set<Long> alreadyIn = result.stream()
                    .map(ArticleCardResponse::articleId)
                    .collect(Collectors.toSet());
            articleMetaRepository
                    .findAll(PageRequest.of(0, CANDIDATE_POOL,
                            org.springframework.data.domain.Sort.by(
                                    org.springframework.data.domain.Sort.Direction.DESC, "publishedAt")))
                    .getContent()
                    .stream()
                    .filter(a -> !readIds.contains(a.getId()) && !alreadyIn.contains(a.getId()))
                    .limit(effectiveLimit - result.size())
                    .map(ArticleCardResponse::from)
                    .forEach(result::add);
        }

        log.debug("[Recommendation] userId={} interest={} weak={} result={}",
                userId, interestCategories.size(), weakCategories.size(), result.size());

        return new ArrayList<>(result);
    }

    private List<ArticleCardResponse> fetchUnread(
            List<ArticleCategory> categories, Set<Long> readIds, int limit) {
        return articleMetaRepository
                .findAll(PageRequest.of(0, CANDIDATE_POOL,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "publishedAt")))
                .getContent()
                .stream()
                .filter(a -> categories.contains(a.getCategory()) && !readIds.contains(a.getId()))
                .limit(limit)
                .map(ArticleCardResponse::from)
                .toList();
    }

    private ArticleCategory toCategory(String name) {
        try {
            return ArticleCategory.from(name);
        } catch (IllegalArgumentException e) {
            // 표시 이름으로 매칭 시도
            for (ArticleCategory c : ArticleCategory.values()) {
                if (c.getDisplayName().equals(name)) return c;
            }
            return null;
        }
    }
}
