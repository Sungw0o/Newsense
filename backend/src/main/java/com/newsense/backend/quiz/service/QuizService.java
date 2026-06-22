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
import com.newsense.backend.quiz.dto.QuizResponse;
import com.newsense.backend.quiz.repository.QuizRepository;
import com.newsense.backend.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final TermRepository termRepository;
    private final OpenAiQuizClient openAiQuizClient;

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
                economicTerms
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
}
