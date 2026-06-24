package com.newsense.backend.user.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.newsense.backend.article.domain.ArticleDifficulty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean isActive;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest", nullable = false)
    private List<String> interests = new ArrayList<>();

    @Column(length = 255)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleDifficulty level;

    @Column(nullable = false, length = 50)
    private String subPlan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = UserRole.USER;
        this.isActive = true;
        this.interests = new ArrayList<>();
        this.profileImageUrl = "";
        this.level = ArticleDifficulty.BASIC;
        this.subPlan = "Standard Plan (Free)";
        this.createdAt = LocalDateTime.now();
    }

    public static User create(String email, String encodedPassword, String nickname) {
        return new User(email, encodedPassword, nickname);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateProfile(String nickname, List<String> interests, String profileImageUrl, ArticleDifficulty level) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (interests != null) {
            this.interests = new ArrayList<>(interests);
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        if (level != null) {
            this.level = level;
        }
    }
}
