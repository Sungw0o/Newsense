package com.newsense.backend.wrongnote.repository;

import com.newsense.backend.wrongnote.domain.WrongNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long>, JpaSpecificationExecutor<WrongNote> {

    @EntityGraph(attributePaths = {"relatedTerms", "quiz"})
    Optional<WrongNote> findByIdAndUserIdAndIsActiveTrue(Long wrongNoteId, Long userId);

    @EntityGraph(attributePaths = {"relatedTerms", "quiz"})
    Optional<WrongNote> findByUserIdAndQuizId(Long userId, Long quizId);

    @EntityGraph(attributePaths = {"relatedTerms"})
    @Query("""
            select note
            from WrongNote note
            where note.user.id = :userId
              and note.isActive = true
              and note.isResolved = false
            order by note.mistakeCount desc, note.lastWrongAt desc
            """)
    List<WrongNote> findWeaknessSignals(@Param("userId") Long userId);
}
