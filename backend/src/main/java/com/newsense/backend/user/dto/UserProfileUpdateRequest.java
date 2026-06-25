package com.newsense.backend.user.dto;

import com.newsense.backend.article.domain.ArticleDifficulty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserProfileUpdateRequest(
        @Size(max = 30) String nickname,
        List<String> interests,
        String profileImageUrl,
        ArticleDifficulty level
) {
}
