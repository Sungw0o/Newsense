package com.newsense.backend.article.converter;

import com.newsense.backend.article.domain.ArticleDifficulty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArticleDifficultyConverter implements Converter<String, ArticleDifficulty> {

    @Override
    public ArticleDifficulty convert(String source) {
        return ArticleDifficulty.from(source.trim());
    }
}
