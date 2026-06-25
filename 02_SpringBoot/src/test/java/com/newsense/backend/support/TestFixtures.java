package com.newsense.backend.support;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.quiz.domain.QuizType;
import com.newsense.backend.user.domain.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user(Long id) {
        User user = User.create("user" + id + "@example.com", "encoded", "tester" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static ArticleMeta article(Long id) {
        ArticleMeta article = ArticleMeta.create(
                "금리 인하와 금융 시장",
                "금융 시장 요약",
                "한국경제",
                "https://example.com/articles/" + id,
                LocalDate.of(2026, 6, 24),
                3,
                "mongo-" + id,
                "hash-" + id
        );
        ReflectionTestUtils.setField(article, "id", id);
        return article;
    }

    public static ArticleContent content(String id, String text) {
        ArticleContent content = ArticleContent.create(
                "source",
                "한국경제",
                "https://example.com/source/" + id,
                "금융 기사",
                LocalDate.of(2026, 6, 24),
                text,
                text,
                List.of(text),
                "hash-" + id
        );
        ReflectionTestUtils.setField(content, "id", id);
        return content;
    }

    public static Quiz quiz(Long id, ArticleMeta article, String correctAnswer) {
        Quiz quiz = Quiz.create(
                article,
                QuizType.MULTIPLE,
                "기준금리의 의미는?",
                List.of("물가", "금리", "환율", "주가"),
                correctAnswer,
                "기준금리는 중앙은행 정책 금리입니다.",
                1
        );
        ReflectionTestUtils.setField(quiz, "id", id);
        return quiz;
    }
}
