package com.newsense.backend.quiz.service;

import com.newsense.backend.ai.quiz.GeneratedQuiz;
import com.newsense.backend.ai.quiz.EconomicTermContext;
import com.newsense.backend.ai.quiz.OpenAiQuizClient;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.quiz.domain.QuizAnswer;
import com.newsense.backend.quiz.dto.QuizAnswerRequest;
import com.newsense.backend.quiz.dto.QuizAnswerResponse;
import com.newsense.backend.quiz.dto.QuizResponse;
import com.newsense.backend.quiz.event.QuizCompletedEvent;
import com.newsense.backend.quiz.repository.QuizAnswerRepository;
import com.newsense.backend.quiz.repository.QuizRepository;
import com.newsense.backend.rag.service.RagRetrievalService;
import com.newsense.backend.term.domain.ArticleTerm;
import com.newsense.backend.term.repository.ArticleTermRepository;
import com.newsense.backend.term.repository.TermRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.service.WrongNoteRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final ArticleTermRepository articleTermRepository;
    private final TermRepository termRepository;
    private final UserRepository userRepository;
    private final OpenAiQuizClient openAiQuizClient;
    private final WrongNoteRecorder wrongNoteRecorder;
    private final RagRetrievalService ragRetrievalService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<QuizResponse> getArticleQuizzes(Long articleId) {
        List<Quiz> quizzes = quizRepository.findByArticleIdAndIsActiveTrueOrderByDisplayOrder(articleId);
        if (quizzes.isEmpty()) {
            quizzes = generateQuizzes(articleId);
        }
        return quizzes.stream().map(QuizResponse::from).toList();
    }

    @Transactional
    public List<Quiz> regenerateQuizzes(Long articleId) {
        List<Quiz> existing = quizRepository.findByArticleIdAndIsActiveTrueOrderByDisplayOrder(articleId);
        existing.forEach(Quiz::deactivate);
        return generateQuizzes(articleId);
    }

    @Transactional
    public List<Quiz> generateIfAbsent(Long articleId) {
        List<Quiz> existing = quizRepository.findByArticleIdAndIsActiveTrueOrderByDisplayOrder(articleId);
        return existing.isEmpty() ? generateQuizzes(articleId) : existing;
    }

    @Transactional
    public QuizAnswerResponse submitAnswer(Long quizId, Long userId, QuizAnswerRequest request) {
        Quiz quiz = quizRepository.findByIdAndIsActiveTrue(quizId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String userAnswer = request.answer().trim();
        boolean correct = isCorrect(userAnswer, quiz.getCorrectAnswer());
        QuizAnswer answer = quizAnswerRepository.save(QuizAnswer.create(user, quiz, userAnswer, correct));

        boolean wrongNoteRecorded = false;
        if (!correct) {
            wrongNoteRecorder.record(userId, quiz, userAnswer, getRelatedTermNames(quiz.getArticle().getId()));
            wrongNoteRecorded = true;
        }

        eventPublisher.publishEvent(new QuizCompletedEvent(
                userId,
                quiz.getArticle().getId(),
                quiz.getId(),
                correct,
                answer.getSubmittedAt()
        ));
        return QuizAnswerResponse.of(answer, quiz, wrongNoteRecorded);
    }

    private List<Quiz> generateQuizzes(Long articleId) {
        ArticleMeta article = articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_CONTENT_NOT_FOUND));

        List<EconomicTermContext> economicTerms = termRepository.findAll().stream()
                .filter(term -> content.getCleanText().contains(term.getName()))
                .map(term -> new EconomicTermContext(term.getName(), term.getDefinition()))
                .toList();
        List<GeneratedQuiz> generated = openAiQuizClient.generate(
                article.getTitle(),
                content.getCleanText(),
                economicTerms,
                ragRetrievalService.retrieveQuizEvidence(article.getTitle(), content, economicTerms)
        );
        List<Quiz> quizzes = new ArrayList<>(generated.size());
        for (int index = 0; index < generated.size(); index++) {
            GeneratedQuiz item = generated.get(index);
            quizzes.add(Quiz.create(
                    article,
                    item.type(),
                    item.question(),
                    item.options(),
                    item.correctAnswer(),
                    item.explanation(),
                    index + 1
            ));
        }
        return quizRepository.saveAll(quizzes);
    }

    private boolean isCorrect(String userAnswer, String correctAnswer) {
        return normalizeAnswer(userAnswer).equals(normalizeAnswer(correctAnswer));
    }

    private String normalizeAnswer(String answer) {
        return Normalizer.normalize(answer.trim(), Normalizer.Form.NFKC)
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private List<String> getRelatedTermNames(Long articleId) {
        return articleTermRepository.findAllByArticleIdWithTerm(articleId).stream()
                .map(ArticleTerm::getTerm)
                .map(term -> term.getName())
                .toList();
    }
}
