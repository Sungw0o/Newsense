package com.newsense.backend.learning.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningHistoryType {

    ARTICLE_READ("기사 읽기"),
    REVIEW("리뷰 작성"),
    QUIZ("퀴즈 완료");

    private final String displayName;
}
