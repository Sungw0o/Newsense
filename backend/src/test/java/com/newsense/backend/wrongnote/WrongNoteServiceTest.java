package com.newsense.backend.wrongnote;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.wrongnote.domain.WrongNote;
import com.newsense.backend.wrongnote.dto.WrongNoteResponse;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import com.newsense.backend.wrongnote.service.WrongNoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("WrongNoteService unit tests")
class WrongNoteServiceTest {

    @InjectMocks
    WrongNoteService wrongNoteService;

    @Mock
    WrongNoteRepository wrongNoteRepository;

    @Mock
    UserRepository userRepository;

    @Test
    void record_createsNewWrongNoteWithNormalizedTerms() {
        User user = TestFixtures.user(7L);
        ArticleMeta article = TestFixtures.article(1L);
        Quiz quiz = TestFixtures.quiz(10L, article, "O");
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(wrongNoteRepository.findByUserIdAndQuizId(7L, 10L)).willReturn(Optional.empty());

        wrongNoteService.record(7L, quiz, " X ", List.of(" 금리 ", "", "금리", "환율"));

        then(wrongNoteRepository).should().save(any(WrongNote.class));
    }

    @Test
    void record_updatesExistingWrongNote() {
        User user = TestFixtures.user(7L);
        ArticleMeta article = TestFixtures.article(1L);
        Quiz quiz = TestFixtures.quiz(10L, article, "O");
        WrongNote note = WrongNote.create(user, quiz, "X", List.of("금리"));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(wrongNoteRepository.findByUserIdAndQuizId(7L, 10L)).willReturn(Optional.of(note));

        wrongNoteService.record(7L, quiz, "X", List.of("환율"));

        assertThat(note.getMistakeCount()).isEqualTo(2);
        assertThat(note.getRelatedTerms()).containsExactly("환율");
        then(wrongNoteRepository).should().save(note);
    }

    @Test
    void toggleResolved_switchesResolvedState() {
        User user = TestFixtures.user(7L);
        WrongNote note = WrongNote.create(user, TestFixtures.quiz(10L, TestFixtures.article(1L), "O"), "X", List.of());
        given(wrongNoteRepository.findByIdAndUserIdAndIsActiveTrue(3L, 7L)).willReturn(Optional.of(note));

        WrongNoteResponse response = wrongNoteService.toggleResolved(3L, 7L);

        assertThat(response.isResolved()).isTrue();
        assertThat(note.getResolvedAt()).isNotNull();
    }

    @Test
    void deleteWrongNote_deactivatesOwnedNote() {
        User user = TestFixtures.user(7L);
        WrongNote note = WrongNote.create(user, TestFixtures.quiz(10L, TestFixtures.article(1L), "O"), "X", List.of());
        given(wrongNoteRepository.findByIdAndUserIdAndIsActiveTrue(3L, 7L)).willReturn(Optional.of(note));

        wrongNoteService.deleteWrongNote(3L, 7L);

        assertThat(note.isActive()).isFalse();
        assertThat(note.getDeletedAt()).isNotNull();
    }

    @Test
    void toggleResolved_throwsWhenNoteMissing() {
        given(wrongNoteRepository.findByIdAndUserIdAndIsActiveTrue(3L, 7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> wrongNoteService.toggleResolved(3L, 7L))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.WRONG_NOTE_NOT_FOUND));
    }
}
