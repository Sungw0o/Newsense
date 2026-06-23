package com.newsense.backend.wrongnote.repository;

import com.newsense.backend.wrongnote.domain.WrongNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long>, JpaSpecificationExecutor<WrongNote> {

    @EntityGraph(attributePaths = {"relatedTerms", "quiz"})
    Optional<WrongNote> findByIdAndUserIdAndIsActiveTrue(Long wrongNoteId, Long userId);

    @EntityGraph(attributePaths = {"relatedTerms", "quiz"})
    Optional<WrongNote> findByUserIdAndQuizId(Long userId, Long quizId);
}
