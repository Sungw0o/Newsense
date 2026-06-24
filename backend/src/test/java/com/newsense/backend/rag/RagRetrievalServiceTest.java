package com.newsense.backend.rag;

import com.newsense.backend.ai.quiz.EconomicTermContext;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.rag.dto.RagRecommendationResponse;
import com.newsense.backend.rag.dto.RagSearchResponse;
import com.newsense.backend.rag.service.RagRetrievalService;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.wrongnote.domain.WrongNote;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagRetrievalService unit tests")
class RagRetrievalServiceTest {

    @InjectMocks
    RagRetrievalService ragRetrievalService;

    @Mock
    ArticleMetaRepository articleMetaRepository;

    @Mock
    ArticleContentRepository articleContentRepository;

    @Mock
    WrongNoteRepository wrongNoteRepository;

    @Test
    void search_returnsRankedArticleMatches() {
        ArticleMeta article = TestFixtures.article(1L);
        ArticleContent content = TestFixtures.content("mongo-1", "기준금리 인하가 금융 시장에 영향을 준다.");
        given(articleMetaRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(article)));
        given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

        RagSearchResponse response = ragRetrievalService.search("기준금리 금융", 10);

        assertThat(response.keywords()).contains("기준금리", "금융");
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst().score()).isPositive();
    }

    @Test
    void search_rejectsBlankQuery() {
        assertThatThrownBy(() -> ragRetrievalService.search(" ", 5))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void recommendForWeakness_returnsEmptyWhenNoWrongNotes() {
        given(wrongNoteRepository.findWeaknessSignals(7L)).willReturn(List.of());

        RagRecommendationResponse response = ragRetrievalService.recommendForWeakness(7L, 5);

        assertThat(response.weaknessTerms()).isEmpty();
        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void recommendForWeakness_searchesArticlesByWrongNoteTerms() {
        ArticleMeta article = TestFixtures.article(1L);
        ArticleContent content = TestFixtures.content("mongo-1", "환율 변동과 금리 정책 기사");
        WrongNote note = WrongNote.create(
                TestFixtures.user(7L),
                TestFixtures.quiz(10L, article, "O"),
                "X",
                List.of("환율", "금리")
        );
        given(wrongNoteRepository.findWeaknessSignals(7L)).willReturn(List.of(note));
        given(articleMetaRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(article)));
        given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));

        RagRecommendationResponse response = ragRetrievalService.recommendForWeakness(7L, 0);

        assertThat(response.weaknessTerms()).containsExactly("환율", "금리");
        assertThat(response.recommendations()).hasSize(1);
    }

    @Test
    void retrieveQuizEvidence_usesChunksWhenKeywordsMatchOrFallsBack() {
        ArticleContent content = TestFixtures.content(
                "mongo-1",
                "기준금리 설명 문장"
        );

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
