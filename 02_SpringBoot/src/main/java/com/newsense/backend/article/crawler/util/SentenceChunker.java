package com.newsense.backend.article.crawler.util;

import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SentenceChunker {

    public List<String> chunk(String text, int targetSize) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        int safeTargetSize = Math.max(targetSize, 200);
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.KOREAN);
        iterator.setText(text);

        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end).trim();
            if (sentence.isEmpty()) {
                continue;
            }
            if (sentence.length() > safeTargetSize) {
                flush(chunks, current);
                splitLongSentence(chunks, sentence, safeTargetSize);
                continue;
            }
            if (!current.isEmpty() && current.length() + sentence.length() + 1 > safeTargetSize) {
                flush(chunks, current);
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(sentence);
        }
        if (!current.isEmpty()) {
            flush(chunks, current);
        }
        return List.copyOf(chunks);
    }

    private void splitLongSentence(List<String> chunks, String sentence, int targetSize) {
        for (int start = 0; start < sentence.length(); start += targetSize) {
            int end = Math.min(start + targetSize, sentence.length());
            chunks.add(sentence.substring(start, end));
        }
    }

    private void flush(List<String> chunks, StringBuilder current) {
        if (!current.isEmpty()) {
            chunks.add(current.toString());
            current.setLength(0);
        }
    }
}
