package com.newsense.backend.quiz.service;

import com.newsense.backend.ai.quiz.GeneratedQuiz;
import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.quiz.EconomicTermContext;
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
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.service.WrongNoteRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    private final QuizCriticClient quizCriticClient;
    private final AiPipelineProperties pipelineProperties;
    private final WrongNoteRecorder wrongNoteRecorder;
    private final RagRetrievalService ragRetrievalService;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoTemplate mongoTemplate;

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
        ArticleContent content = getArticleContent(article)
                .orElse(null);
        String articleText = content == null ? article.getSummary() : content.getCleanText();

        List<EconomicTermContext> economicTerms = termRepository.findAll().stream()
                .filter(term -> articleText.contains(term.getName()))
                .map(term -> new EconomicTermContext(term.getName(), term.getDefinition()))
                .toList();
        List<GeneratedQuiz> generated = generateWithAiOrFallback(article, content, articleText, economicTerms);
        List<Quiz> quizzes = new ArrayList<>(generated.size());
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
                    index + 1
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
        try {
            List<String> evidence = content == null
                    ? List.of(article.getSummary())
                    : ragRetrievalService.retrieveQuizEvidence(article.getTitle(), content, economicTerms);
            return generateAndCritique(article, articleText, economicTerms, evidence);
        } catch (RuntimeException exception) {
            return fallbackQuizzes(article, economicTerms);
        }
    }

    private List<GeneratedQuiz> generateAndCritique(
            ArticleMeta article,
            String articleText,
            List<EconomicTermContext> economicTerms,
            List<String> evidence
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
                        criticFeedback
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
            }
        }
        if (lastFailure != null) {
            throw lastFailure;
        }
        throw new CustomException(ErrorCode.QUIZ_GENERATION_FAILED);
    }

    private List<GeneratedQuiz> fallbackQuizzes(
            ArticleMeta article,
            List<EconomicTermContext> economicTerms
    ) {
        String categoryName = article.getCategory().getDisplayName();
        String keyword = economicTerms.isEmpty() ? "핵심 경제 개념" : economicTerms.get(0).name();
        return List.of(
                new GeneratedQuiz(
                        QuizType.OX,
                        QuizPurpose.BASIC_CONCEPT,
                        "이 기사는 " + categoryName + " 흐름을 이해하는 데 필요한 내용을 다룬다.",
                        List.of("O", "X"),
                        "O",
                        "기사의 분류와 본문 요약을 바탕으로 해당 경제 영역의 주요 흐름을 설명하는 문제입니다."
                ),
                new GeneratedQuiz(
                        QuizType.MULTIPLE,
                        QuizPurpose.FACT_CHECK,
                        "기사에서 가장 먼저 확인해야 할 핵심 개념은 무엇인가요?",
                        List.of(keyword, "운동 경기 결과", "연예 일정", "날씨 예보"),
                        keyword,
                        "본문에 포함된 경제 용어와 기사 분류를 기준으로 핵심 개념을 고르는 문제입니다."
                ),
                new GeneratedQuiz(
                        QuizType.MULTIPLE,
                        QuizPurpose.CAUSAL_REASONING,
                        "이 기사를 읽을 때 적절한 학습 관점은 무엇인가요?",
                        List.of("원인과 시장 영향을 함께 파악한다", "제목만 보고 결론을 확정한다", "본문 수치를 모두 무시한다", "기사 출처를 확인하지 않는다"),
                        "원인과 시장 영향을 함께 파악한다",
                        "경제 기사는 사건의 원인, 지표 변화, 시장 또는 정책 영향을 연결해서 읽는 것이 중요합니다."
                )
        );
    }

    private boolean isCorrect(String userAnswer, String correctAnswer) {
        return normalizeAnswer(userAnswer).equals(normalizeAnswer(correctAnswer));
    }

    private String normalizeAnswer(String answer) {
        return Normalizer.normalize(answer.trim(), Normalizer.Form.NFKC)
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private List<String> getRe