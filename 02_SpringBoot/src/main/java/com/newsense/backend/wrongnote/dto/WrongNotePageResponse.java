package com.newsense.backend.wrongnote.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record WrongNotePageResponse(
        List<WrongNoteResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static WrongNotePageResponse from(Page<WrongNoteResponse> notes) {
        return new WrongNotePageResponse(
                notes.getContent(),
                notes.getNumber(),
                notes.getSize(),
                notes.getTotalElements(),
                notes.getTotalPages(),
                notes.isLast()
        );
    }
}
