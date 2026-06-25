package com.newsense.backend.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "article_related_stock",
        indexes = @Index(name = "idx_related_stock_article_id", columnList = "article_meta_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleRelatedStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_meta_id", nullable = false)
    private ArticleMeta articleMeta;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "relation_reason", length = 500)
    private String relationReason;

    public static ArticleRelatedStock create(ArticleMeta articleMeta, String stockName, String stockCode, String relationReason) {
        ArticleRelatedStock stock = new ArticleRelatedStock();
        stock.articleMeta = articleMeta;
        stock.stockName = stockName;
        stock.stockCode = stockCode;
        stock.relationReason = relationReason;
        return stock;
    }
}
