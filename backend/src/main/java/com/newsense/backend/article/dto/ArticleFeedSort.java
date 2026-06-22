package com.newsense.backend.article.dto;

import org.springframework.data.domain.Sort;

public enum ArticleFeedSort {
    LATEST(Sort.by(
            Sort.Order.desc("publishedAt").nullsLast(),
            Sort.Order.desc("id")
    ));

    private final Sort sort;

    ArticleFeedSort(Sort sort) {
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }
}
