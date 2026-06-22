package com.newsense.backend.article.converter;

import com.newsense.backend.article.domain.ArticleCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArticleCategoryConverter implements Converter<String, ArticleCategory> {

    @Override
    public ArticleCategory convert(String source) {
        return ArticleCategory.from(source.trim());
    }
}
