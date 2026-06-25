package com.newsense.backend.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewCreateRequest(
        @NotNull @Positive Long articleId,
        @NotBlank @Size(max = 2000) String summary,
        @NotBlank @Size(max = 2000) String learned,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> difficultTerms
) {
    public ReviewCreateRequest {
        difficultTerms = difficultTerms == null ? List.of() : List.copyOf(difficultTerms);
    }
}
