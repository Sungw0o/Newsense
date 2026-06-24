package com.newsense.backend.community.dto;

import org.springframework.data.domain.Sort;

public enum PostSort {
    LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    LIKES(Sort.by(Sort.Direction.DESC, "likes").and(Sort.by(Sort.Direction.DESC, "createdAt"))),
    VIEWS(Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "createdAt")));

    private final Sort sort;

    PostSort(Sort sort) {
        this.sort = sort;
    }

    public Sort toSort() {
        return sort;
    }
}
