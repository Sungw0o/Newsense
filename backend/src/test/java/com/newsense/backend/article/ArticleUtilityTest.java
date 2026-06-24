package com.newsense.backend.article;

import com.newsense.backend.article.converter.ArticleCategoryConverter;
import com.newsense.backend.article.converter.ArticleCategoryJpaConverter;
import com.newsense.backend.article.converter.ArticleDifficultyConverter;
import com.newsense.backend.article.crawler.util.ContentCleaner;
import com.newsense.backend.article.crawler.util.ContentHasher;
import com.newsense.backend.article.crawler.util.SentenceChunker;
import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Article utility unit tests")
class ArticleUtilityTest {

    @Test
    void contentCleaner_removesHtmlAndInvisibleSpaces() {
        ContentCleaner cleaner = new ContentCleaner();

        String result = cleaner.clean("<p> 기준&nbsp;금리 </p>\u200B\t<span>인하</span>");

        assertThat(result).isEqualTo("기준 금리 인하");
        assertThat(cleaner.clean("  ")).isEmpty();
        assertThat(cleaner.clean(null)).isEmpty();
    }

    @Test
    void contentHasher_returnsStableSha256() {
        ContentHasher hasher = new ContentHasher();

        assertThat(hasher.sha256("newsense"))
                .isEqualTo(hasher.sha256("newsense"))
                .hasSize(64);
        assertThat(hasher.sha256("newsense")).isNotEqualTo(hasher.sha256("other"));
    }

    @Test
    void sentenceChunker_splitsEmptyShortAndLongText() {
        SentenceChunker chunker = new SentenceChunker();

        assertThat(chunker.chunk(null, 100)).isEmpty();
        assertThat(chunker.chunk("첫 문장입니다. 두 번째 문장입니다.", 200))
                .containsExactly("첫 문장입니다. 두 번째 문장입니다.");

        String longSentence = "가".repeat(450);
        List<String> chunks = chunker.chunk(longSentence, 100);

        assertThat(chunks).hasSize(3);
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.length()).isLessThanOrEqualTo(200));
    }

    @Test
    void converters_acceptEnumNameAndRejectUnsupportedValue() {
        ArticleCategoryConverter categoryConverter = new ArticleCategoryConverter();
        ArticleDifficultyConverter difficultyConverter = new ArticleDifficultyConverter();

        assertThat(categoryConverter.convert("finance")).isEqualTo(ArticleCategory.FINANCE_INVESTMENT);
        assertThat(difficultyConverter.convert("BASIC")).isEqualTo(ArticleDifficulty.BASIC);
        assertThatThrownBy(() -> categoryConverter.convert("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> difficultyConverter.convert("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void articleCategoryJpaConverter_acceptsLegacyDatabaseValues() {
        ArticleCategoryJpaConverter converter = new ArticleCategoryJpaConverter();

        assertThat(converter.convertToEntityAttribute("ECONOMY")).isEqualTo(ArticleCategory.MACRO_ECONOMY);
        assertThat(converter.convertToEntityAttribute("FINANCE")).isEqualTo(ArticleCategory.FINANCE_INVESTMENT);
        assertThat(converter.convertToDatabaseColumn(ArticleCategory.POLICY_SYSTEM)).isEqualTo("POLICY_SYSTEM");
    }
}
