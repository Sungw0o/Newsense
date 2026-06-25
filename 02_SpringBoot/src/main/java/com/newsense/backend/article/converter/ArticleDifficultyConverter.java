package com.newsense.backend.article.converter;

import com.newsense.backend.article.domain.ArticleDifficulty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArticleDifficultyConverter implements Converter<String, ArticleDifficulty> {

    @Override
    public ArticleDifficulty convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        return ArticleDifficulty.from(source.trim());
    }
}
