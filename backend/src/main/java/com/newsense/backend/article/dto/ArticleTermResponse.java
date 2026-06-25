package com.newsense.backend.article.dto;

import com.newsense.backend.term.domain.Term;

public record ArticleTermResponse(Long id, String name, String definition, String source) {

    public static ArticleTermResponse from(Term term) {
        return new ArticleTermResponse(term.getId(), term.getName(), term.getDefinition(), term.getSource());
    }
}
