package com.newsense.backend.quiz;

import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.quiz.GeneratedQuiz;
import com.newsense.backend.ai.quiz.OpenAiQuizClient;
import com.newsense.backend.ai.quiz.QuizCriticClient;
import com.newsense.backend.ai.quiz.QuizCritiqueResult;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.quiz.domain.QuizAnswer;
import com.newsense.backend.quiz.domain.QuizType;
import com.newsense.backend.quiz.dto.QuizAnswerRequest;
import com.newsense.backend.quiz.dto.QuizAnswerResponse;
import com.newsense.backend.quiz.dto.QuizResponse;
import com.newsense.backend.quiz.repository.QuizAnswerRepository;
import com.newsense.backend.quiz.repository.QuizRepository;
import com.newsense.backend.quiz.service.QuizService;
import com.newsense.backend.rag.service.RagRetrievalService;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.term.domain.Term;
import com.newsense.backend.term.repository.ArticleTermRepository;
import com.newsense.backend.term.repository.TermRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.service.WrongNoteRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService unit tests")
class QuizServiceTest {

    @InjectMocks
    QuizService quizService;

    @Mock
    QuizRepository quizRepository;

    @Mock
    QuizAnswerRepository quizAnswerRepository;

    @Mock
    ArticleMetaRepository articleMetaRepository;

    @Mock
    ArticleContentRepository articleContentRepository;

    @Mock
    ArticleTermRepository articleTermRepository;

    @Mock
    TermRepository termRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    OpenAiQuizClient openAiQuizClient;

    @Mock
    WrongNoteRecorder wrongNoteRecorder;

    @Mock
    RagRetrievalService ragRetrievalService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    QuizCriticClient quizCriticClient;

    @Mock
    AiPipelineProperties pipelineProperties;

    @Mock
    MongoTemplate mongoTemplate;

    @Test
    void getArticleQuizzes_returnsExistingQuizzesWithoutAiCall() {
        ArticleMeta article = TestFixtures.article(1L);
        Quiz quiz = TestFixtures.quiz(10L, article, "금리");
        given(quizRepository.findByArticleIdAndIsActiveTrueOrderByDisplayOrder(1L))
                .willReturn(List.of(quiz));

        List<QuizResponse> responses = quizService.getArticleQuizzes(1L);

        assertThat(responses).hasSize(1);
        then(openAiQuizClient).shouldHaveNoInteractions();
    }

    @Test
    void getArticleQuizzes_generatesWhenAbsent() {
        ArticleMeta article = TestFixtures.article(1L);
        ArticleContent content = TestFixtures.content("mongo-1", "기준금리와 환율 설명");
        given(quizRepository.findByArticleIdAndIsActiveTrueOrderByDisplayOrder(1L)).willReturn(List.of());
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(articleContentRepository.findById("mongo-1")).willReturn(Optional.of(content));
        given(termRepository.findAll()).willReturn(List.of(Term.create("기준금리", "정책 금리", "BOK")));
        given(pipelineProperties.maxQuizRetries()).willReturn(1);
        given(ragRetrievalService.retrieveQuizEvidence(anyString(), any(), anyList()))
                .willReturn(List.of("근거 문장"));
        given(openAiQuizClient.generate(anyString(), anyString(), anyList(), anyList(), isNull()))
                .willReturn(List.of(new GeneratedQuiz(
                        QuizType.OX,
                        "기준금리는 정책 금리다.",
                        List.of("O", "X"),
                        "O",
                        "중앙은행 정책 금리입니다."
                )));
        given(quizCriticClient.critique(anyString(), anyString(), anyList(), anyList()))
                .willReturn(QuizCritiqueResult.approvedResult());
        given(quizRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        List<QuizResponse> responses = quizService.getArticleQuizzes(1L);

        assertThat(responses).hasSize(1);
        then(quizRepository).should().saveAll(anyList());
    }

    @Test
    void submitAnswer_publishesEventWhenCorrect() {
        ArticleMeta article = TestFixtures.article(1L);
        Quiz quiz = TestFixtures.quiz(10L, article, "O");
        User user = TestFixtures.user(7L);
        given(quizRepository.findByIdAndIsActiveTrue(10L)).willReturn(Optional.of(quiz));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(quizAnswerRepository.save(any(QuizAnswer.class))).willAnswer(invocation -> invocation.getArgument(0));

        QuizAnswerResponse response = quizService.submitAnswer(10L, 7L, new QuizAnswerRequest("  o  "));

        assertThat(response.correct()).isTrue();
        assertThat(response.wrongNoteRecorded()).isFalse();
        then(eventPublisher).should().publishEvent(any(Object.class));
        then(wrongNoteRecorder).shouldHaveNoInteractions();
    }

    @Test
    void submitAnswer_recordsWrongNoteWhenIncorrect() {
        ArticleMeta article = TestFixtures.article(1L);
        Quiz quiz = TestFixtures.quiz(10L, article, "O");
        User user = TestFixtures.user(7L);
        given(quizRepository.findByIdAndIsActiveTrue(10L)).willReturn(Optional.of(quiz));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(quizAnswerRepository.save(any(QuizAnswer.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(articleTermRepository.findAllByArticleIdWithTerm(1L)).willReturn(List.of());

        QuizAnswerResponse response = quizService.submitAnswer(10L, 7L, new QuizAnswerRequ