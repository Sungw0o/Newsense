package com.newsense.backend.wrongnote.service;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.domain.WrongNote;
import com.newsense.backend.wrongnote.dto.WrongNotePageResponse;
import com.newsense.backend.wrongnote.dto.WrongNoteResponse;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongNoteService implements WrongNoteRecorder {

    private final WrongNoteRepository wrongNoteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public WrongNotePageResponse getWrongNotes(
            Long userId,
            ArticleCategory category,
            Boolean isResolved,
            int page,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastWrongAt"));
        Page<WrongNoteResponse> notes = wrongNoteRepository
                .findAll(createSpecification(userId, category, isResolved), pageRequest)
                .map(WrongNoteResponse::from);
        return WrongNotePageResponse.from(notes);
    }

    @Override
    @Transactional
    public void record(Long userId, Quiz quiz, String userAnswer, List<String> relatedTerms) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        List<String> normalizedTerms = normalizeTerms(relatedTerms);

        WrongNote note = wrongNoteRepository.findByUserIdAndQuizId(userId, quiz.getId())
                .map(existing -> {
                    existing.recordAgain(userAnswer.trim(), normalizedTerms);
                    return existing;
                })
                .orElseGet(() -> WrongNote.create(user, quiz, userAnswer.trim(), normalizedTerms));
        wrongNoteRepository.save(note);
    }

    @Transactional
    public WrongNoteResponse toggleResolved(Long wrongNoteId, Long userId) {
        WrongNote note = getOwnedNote(wrongNoteId, userId);
        note.toggleResolved();
        return WrongNoteResponse.from(note);
    }

    @Transactional
    public void deleteWrongNote(Long wrongNoteId, Long userId) {
        WrongNote note = getOwnedNote(wrongNoteId, userId);
        note.deactivate();
    }

    private WrongNote getOwnedNote(Long wrongNoteId, Long userId) {
        return wrongNoteRepository.findByIdAndUserIdAndIsActiveTrue(wrongNoteId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WRONG_NOTE_NOT_FOUND));
    }

    private Specification<WrongNote> createSpecification(
            Long userId,
            ArticleCategory category,
            Boolean isResolved
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("user").get("id"), userId),
                    criteriaBuilder.isTrue(root.get("isActive"))
            );
            if (category != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), category));
            }
            if (isResolved != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("isResolved"), isResolved)
                );
            }
            return predicate;
        };
    }

    private List<String> normalizeTerms(List<String> relatedTerms) {
        if (relatedTerms == null) {
            return List.of();
        }
        return relatedTerms.stream()
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .distinct()
                .limit(20)
                .toList();
    }
}
