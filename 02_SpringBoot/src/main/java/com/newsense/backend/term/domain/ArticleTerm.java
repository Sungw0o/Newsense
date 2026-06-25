package com.newsense.backend.term.domain;

import com.newsense.backend.article.domain.ArticleMeta;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "article_term",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_article_term_article_term",
                columnNames = {"article_id", "term_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    public static ArticleTerm create(ArticleMeta article, Term term) {
        ArticleTerm articleTerm = new ArticleTerm();
        articleTerm.article = article;
        articleTerm.term = term;
        return articleTerm;
    }
}
