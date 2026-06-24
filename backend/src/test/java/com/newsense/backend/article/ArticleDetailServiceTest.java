package com.newsense.backend.article;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.domain.ArticleRead;
import com.newsense.backend.article.dto.ArticleDetailResponse;
import com.newsense.backend.article.dto.ArticleReadResponse;
import com.newsense.backend.article.dto.ArticleTermResponse;
import com.newsense.backend.article.dto.BookmarkToggleResponse;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.repository.ArticleReadRepository;
import com.newsense.backend.article.service.ArticleDetailService;
import com.newsense.backend.bookmark.domain.Bookmark;
import com.newsense.backend.bookmark.repository.BookmarkRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.term.domain.Term;
import com.newsense.backend.term.repository.ArticleTermRepository;
import com.newsense.backend.term.repository.TermRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleDetailService unit tests")
class ArticleDetailServiceTest {

    @InjectMocks
    ArticleDetailService articleDetailService;

    @Mock
    ArticleMetaRepository articleMetaRepository;

    @Mock
    ArticleContentRepository articleContentRepository;

    @Mock
    ArticleTermRepository articleTermRepository;

    @Mock
    TermRepository termRepository;

    @Mock
    ArticleReadRepository articleReadRepository;

    @Mock
    BookmarkRepository bookmarkRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void getArticleDetail_returnsContentAndUserFlags() {
        ArticleMeta article = TestFixtures.article(1L);
        ArticleContent content = TestFixtures.content("mongo-1", "기준금리 본문");
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));
        given(articleReadRepository.existsByUserIdAndArticleId(7L, 1L)).willReturn(true);
        given(bookmarkRepository.existsByUserIdAndArticleId(7L, 1L)).willReturn(true);

        ArticleDetailResponse response = articleDetailService.getArticleDetail(1L, 7L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.isRead()).isTrue();
        assertThat(response.isBookmarked()).isTrue();
    }

    @Test
    void getArticleDetail_throwsWhenContentMissing() {
        ArticleMeta article = TestFixtures.article(1L);
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(articleContentRepository.findById("mongo-1")).willReturn(Optional.empty());

        assertThatThrownBy(() -> articleDetailService.getArticleDetail(1L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.ARTICLE_CONTENT_NOT_FOUND));
    }

    @Test
    void getArticleTerms_extractsAndStoresTermsWhenCacheEmpty() {
        ArticleMeta article = TestFixtures.article(1L);
        ArticleContent content = TestFixtures.content("mongo-1", "기준금리와 환율을 설명하는 기사입니다.");
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(articleTermRepository.findAllByArticleIdWithTerm(1L)).willReturn(List.of());
        given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));
        given(termRepository.findAll()).willReturn(List.of(
                Term.create("환율", "외화 교환 비율", "BOK"),
                Term.create("기준금리", "중앙은행 정책 금리", "BOK"),
                Term.create("주가", "주식 가격", "BOK")
        ));
        given(articleTermRepository.saveAll(any())).willAnswer(invocation -> invocation.getArgument(0));

        List<ArticleTermResponse> responses = articleDetailService.getArticleTerms(1L);

        assertThat(responses).extracting(ArticleTermResponse::name)
                .containsExactly("기준금리", "환율");
        then(articleTermRepository).should().saveAll(any());
    }

    @Test
    void markAsRead_savesOnlyFirstReadAndPublishesEvent() {
        ArticleMeta article = TestFixtures.article(1L);
        User user = TestFixtures.user(7L);
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(articleReadRepository.findByUserIdAndArticleId(7L, 1L)).willReturn(Optional.empty());
        given(articleReadRepository.save(any(ArticleRead.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArticleReadResponse response = articleDetailService.markAsRead(1L, 7L);

        assertThat(response.articleId()).isEqualTo(1L);
        assertThat(response.isRead()).isTrue();
        then(eventPublisher).should().publishEvent(any(Object.class));
    }

    @Test
    void toggleBookmark_createsThenDeletesBookmark() {
        ArticleMeta article = TestFixtures.article(1L);
        User user = TestFixtures.user(7L);
        Bookmark bookmark = Bookmark.create(user, article);
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(bookmarkRepository.findByUserIdAndArticleId(7L, 1L))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(bookmark));

        BookmarkToggleResponse created = articleDetailService.toggleBookmark(1L, 7L);
        BookmarkToggleResponse deleted = articleDetailService.toggleBookmark(1L, 7L);

        assertThat(created.isBookmarked()).isTrue();
        assertThat(deleted.isBookmarked()).isFalse();
        then(bookmarkRepository).should().save(any(Bookmark.class));
        then(bookmarkRepository).should().delete(bookmark);
    }
}
