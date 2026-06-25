package com.newsense.backend.learning.service;

import com.newsense.backend.learning.dto.WeaknessSummaryResponse;
import com.newsense.backend.learning.dto.WeaknessSummaryResponse.WeakCategory;
import com.newsense.backend.learning.dto.WeaknessSummaryResponse.WeakTerm;
import com.newsense.backend.wrongnote.domain.WrongNote;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserWeaknessService {

    private static final int TOP_TERMS_LIMIT      = 10;
    private static final int TOP_CATEGORIES_LIMIT = 5;

    private final WrongNoteRepository wrongNoteRepository;

    @Transactional(readOnly = true)
    public WeaknessSummaryResponse getWeaknessSummary(Long userId) {
        List<WrongNote> notes = wrongNoteRepository.findWeaknessSignals(userId);

        if (notes.isEmpty()) {
            return new WeaknessSummaryResponse(List.of(), List.of(), 0, 0.0);
        }

        // 용어별 오답 횟수 집계
        Map<String, Integer> termMistakes = new HashMap<>();
        Map<String, Long> categoryCounts = new HashMap<>();
        long totalMistakeSum = 0;

        for (WrongNote note : notes) {
            // 카테고리 집계
            if (note.getCategory() != null) {
                categoryCounts.merge(note.getCategory().name(), 1L, Long::sum);
            }
            totalMistakeSum += note.getMistakeCount();

            // 연관 용어별 오답 합산
            for (String term : note.getRelatedTerms()) {
                if (term == null || term.isBlank()) continue;
                termMistakes.merge(term.trim(), note.getMistakeCount(), Integer::sum);
            }
        }

        List<WeakTerm> topTerms = termMistakes.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_TERMS_LIMIT)
                .map(e -> new WeakTerm(e.getKey(), e.getValue()))
                .toList();

        List<WeakCategory> topCategories = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_CATEGORIES_LIMIT)
                .map(e -> new WeakCategory(e.getKey(), e.getValue()))
                .toList();

        double avgMistakes = (double) totalMistakeSum / notes.size();

        return new WeaknessSummaryResponse(topTerms, topCategories, notes.size(), avgMistakes);
    }
}
