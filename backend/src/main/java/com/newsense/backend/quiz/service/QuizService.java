package com.newsense.backend.quiz.service;

import com.newsense.backend.ai.quiz.GeneratedQuiz;
import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.quiz.EconomicTermContext;
import com.newsense.backend.ai.quiz.OpenAiQuizClient;
import com.newsense.backend.ai.quiz.QuizCriticClient;
import com.newsense.backend.ai.quiz.QuizCritiqueResult;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.quiz.domain.QuizAnswer;
import com.newsense.backend.quiz.domain.QuizPurpose;
import com.newsense.backend.quiz.domain.QuizType;
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
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.service.WrongNoteRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Duration QUIZ_CACHE_TTL    = Duration.ofHours(24);
    private static final String   QUIZ_CACHE_PREFIX = "quiz:level:";

    private final QuizRepository quizRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final ArticleTermRepository articleTermRepository;
    private final TermRepository termRepository;
    private final UserRepository userRepository;
    private final OpenAiQuizClient openAiQuizClient;
    private final QuizCriticClient quizCriticClient;
    private final AiPipelineProperties pipelineProperties;
    private final WrongNoteRecorder wrongNoteRecorder;
    private final RagRetrievalService ragRetrievalService;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<QuizResponse> getArticleQuizzes(Long articleId) {
        return getArticleQuizzes(articleId, ArticleDifficulty.BASIC);
    }

    @Transactional
    public List<QuizResponse> getArticleQuizzes(Long articleId, UserPrincipal principal) {
        ArticleDifficulty level = principal == null ? ArticleDifficulty.BASIC
                : userRepository.findById(principal.id())
                        .map(User::getLevel)
                        .orElse(ArticleDifficulty.BASIC);
        return getArticleQuizzes(articleId, level);
    }

    @Transactional
    public List<QuizResponse> getArticleQuizzes(Long articleId, ArticleDifficulty userLevel) {
        ArticleDifficulty level = userLevel != null ? userLevel : ArticleDifficulty.BASIC;

        // 1. Redis 캐시 확인
        String cacheKey = QUIZ_CACHE_PREFIX + articleId + ":" + level.name();
        List<QuizResponse> cached = loadFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. DB 조회
        List<Quiz> quizzes = level == ArticleDifficulty.BASIC
                ? quizRepository.findByArticleIdAndIsActiveTrueOrderByDisplayOrder(articleId)
                : quizRepository.findByArticleIdAndUserLevelAndIsActiveTrueOrderByDisplayOrder(articleId, level);

        // 3. 없으면 생성
        if (quizzes.isEmpty()) {
            quizzes = generateQuizzes(articleId, level);
        }

        List<QuizResponse> responses = quizzes.stream().map(QuizResponse::from).toList();
        saveToCache(cacheKey, responses);
        return responses;
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
        return generateQuizzes(articleId, ArticleDifficulty.BASIC);
    }

    private List<Quiz> generateQuizzes(Long articleId, ArticleDifficulty userLevel) {
        ArticleMeta article = articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        ArticleContent content = getArticleContent(article)
                .orElse(null);
        String articleText = content == null ? article.getSummary() : content.getCleanText();

        List<EconomicTermContext> economicTerms = termRepository.findAll().stream()
                .filter(term -> articleText.contains(term.getName()))
                .map(term -> new EconomicTermContext(term.getName(), term.getDefinition()))
                .toList();
        List<GeneratedQuiz> generated = generateWithAiOrFallback(article, content, articleText, economicTerms, userLevel);
        List<Quiz> quizzes = new ArrayList<>(generated.size());
        ArticleDifficulty storedLevel = userLevel == ArticleDifficulty.BASIC ? null : userLevel;
        for (int index = 0; index < generated.size(); index++) {
            GeneratedQuiz item = generated.get(index);
            quizzes.add(Quiz.create(
                    article,
                    item.type(),
                    item.purpose(),
                    item.question(),
                    item.options(),
                    item.correctAnswer(),
                    item.explanation(),
                    index + 1,
                    storedLevel
            ));
        }
        return quizRepository.saveAll(quizzes);
    }

    private Optional<ArticleContent> getArticleContent(ArticleMeta article) {
        return articleContentRepository.findById(article.getMongoDocumentId())
                .or(() -> articleContentRepository.findBySourceUrl(article.getSourceUrl()))
                .or(() -> findArticleContentWithMongoTemplate(article));
    }

    private Optional<ArticleContent> findArticleContentWithMongoTemplate(ArticleMeta article) {
        ArticleContent byId = mongoTemplate.findById(article.getMongoDocumentId(), ArticleContent.class);
        if (byId != null) {
            return Optional.of(byId);
        }
        Query query = Query.query(Criteria.where("sourceUrl").is(article.getSourceUrl()));
        return Optional.ofNullable(mongoTemplate.findOne(query, ArticleContent.class));
    }

    private List<GeneratedQuiz> generateWithAiOrFallback(
            ArticleMeta article,
            ArticleContent content,
            String articleText,
            List<EconomicTermContext> economicTerms
    ) {
        return generateWithAiOrFallback(article, content, articleText, economicTerms, ArticleDifficulty.BASIC);
    }

    private List<GeneratedQuiz> generateWithAiOrFallback(
            ArticleMeta article,
            ArticleContent content,
            String articleText,
            List<EconomicTermContext> economicTerms,
            ArticleDifficulty userLevel
    ) {
        try {
            List<String> evidence = content == null
                    ? List.of(article.getSummary())
                    : ragRetrievalService.retrieveQuizEvidence(article.getTitle(), content, economicTerms);
            return generateAndCritique(article, articleText, economicTerms, evidence, userLevel);
        } catch (RuntimeException exception) {
            return fallbackQuizzes(article, economicTerms);
        }
    }

    private List<GeneratedQuiz> generateAndCritique(
            ArticleMeta article,
            String articleText,
            List<EconomicTermContext> economicTerms,
            List<String> evidence,
            ArticleDifficulty userLevel
    ) {
        String criticFeedback = null;
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= pipelineProperties.maxQuizRetries(); attempt++) {
            try {
                List<GeneratedQuiz> generated = openAiQuizClient.generate(
                        article.getTitle(),
                        articleText,
                        economicTerms,
                        evidence,
                        criticFeedback,
                        userLevel
                );
                QuizCritiqueResult critique = quizCriticClient.critique(
                        article.getTitle(),
                        articleText,
                        evidence,
                        generated
                );
                if (critique.approved()) {
                    return generated;
                }
                criticFeedback = critique.feedback();
            } catch (RuntimeException exception) {
                lastFailure = exception;
                criticFeedback = "생성 또는 검증 중 오류가 발생했습니다: " + exception.getMessage();
            