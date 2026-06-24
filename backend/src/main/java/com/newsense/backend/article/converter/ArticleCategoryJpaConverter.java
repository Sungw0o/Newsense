package com.newsense.backend.article.converter;

import com.newsense.backend.article.domain.ArticleCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ArticleCategoryJpaConverter implements AttributeConverter<ArticleCategory, String> {

    @Override
    public String convertToDatabaseColumn(ArticleCategory attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ArticleCategory convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ArticleCategory.from(dbData);
    }
}
