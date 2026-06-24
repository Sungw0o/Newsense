package com.newsense.backend.article.crawler.util;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ContentCleaner {

    // \uAE30\uC790 \uC774\uBA54\uC77C
    private static final Pattern EMAIL = Pattern.compile(
            "[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE);

    // \uC800\uC791\uAD8C\u00B7\uC7AC\uBC30\uD3EC \uAE08\uC9C0 \uC0C1\uC6A9\uAD6C \uB77C\uC778 \uC804\uCCB4
    private static final Pattern COPYRIGHT_LINE = Pattern.compile(
            "^.*?(Copyrights?|\u00A9|\u24D2|\uBB34\uB2E8\\s*\uC804\uC7AC|\uBB34\uB2E8\\s*\uBC30\uD3EC|\uC7AC\uBC30\uD3EC\\s*\uAE08\uC9C0|\uC800\uC791\uAD8C\uC790|All\\s+rights?\\s+reserved).*$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    // \uAE30\uC790\uBA85\u00B7\uBC30\uD3EC\uCC98 \uC811\uB450/\uC811\uBBF8 \uD328\uD134: [\uC11C\uC6B8=\uB274\uC2DC\uC2A4 \uD64D\uAE38\uB3D9 \uAE30\uC790], (\uC11C\uC6B8=\uC5F0\uD569\uB274\uC2A4), \uD64D\uAE38\uB3D9 \uAE30\uC790 =
    private static final Pattern BYLINE_BRACKET = Pattern.compile(
            "[\\[\\(][^\\[\\]()\n]{0,40}?(?:\uAE30\uC790|\uBC30\uD3EC|\uD2B9\uD30C\uC6D0|=\uB274\uC2A4|=\uC5F0\uD569)[^\\[\\]()\n]{0,40}?[\\]\\)]");

    // "\uD64D\uAE38\uB3D9 \uAE30\uC790" \uB2E8\uB3C5 \uD328\uD134 (\uC904 \uC55E/\uB4A4 \uACBD\uACC4)
    private static final Pattern BYLINE_INLINE = Pattern.compile(
            "(?:^|\\s)[\uAC00-\uD7A3]{2,5}\\s+(?:\uAE30\uC790|\uD2B9\uD30C\uC6D0)(?=\\s|$|[=,])");

    // "= \uD64D\uAE38\uB3D9" \uD615\uD0DC\uC758 \uC11C\uBA85 (\uC5F0\uD569\uB274\uC2A4 \uB4F1)
    private static final Pattern BYLINE_EQUAL = Pattern.compile(
            "(?:^|\\s)=\\s*[\uAC00-\uD7A3]{2,5}(?=\\s|$)");

    public String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String text = Jsoup.parse(rawText).text();

        // \uC720\uB2C8\uCF54\uB4DC \uACF5\uBC31 \uC815\uADDC\uD654
        text = text.replace('\u00A0', ' ')
                   .replace("\u200B", "")
                   .replaceAll("[\\t\\x0B\\f\\r ]+", " ")
                   .replaceAll(" *\\n *", "\n");

        // \uAE30\uC790 \uC774\uBA54\uC77C \uC81C\uAC70
        text = EMAIL.matcher(text).replaceAll("");

        // \uC800\uC791\uAD8C\u00B7\uC7AC\uBC30\uD3EC \uAE08\uC9C0 \uB77C\uC778 \uC81C\uAC70
        text = COPYRIGHT_LINE.matcher(text).replaceAll("");

        // \uAE30\uC790\uBA85/\uBC30\uD3EC\uCC98 \uAD04\uD638 \uD328\uD134 \uC81C\uAC70
        text = BYLINE_BRACKET.matcher(text).replaceAll("");

        // \uC778\uB77C\uC778 \uAE30\uC790 \uC11C\uBA85 \uC81C\uAC70
        text = BYLINE_INLINE.matcher(text).replaceAll(" ");
        text = BYLINE_EQUAL.matcher(text).replaceAll(" ");

        // \uBE48 \uC904 \uC815\uADDC\uD654: 3\uC904 \uC774\uC0C1 \uC5F0\uC18D \uBE48 \uC904 \u2192 \uCD5C\uB300 2\uC904
        text = text.replaceAll("(\n){3,}", "\n\n");

        // \uAC01 \uC904 \uC55E\uB4A4 \uACF5\uBC31 \uC81C\uAC70 \uD6C4 \uC644\uC804\uD788 \uBE48 \uC904 \uC555\uCD95
        text = Arrays.stream(text.split("\n"))
                .map(String::strip)
                .collect(Collectors.joining("\n"));

        return text.replaceAll("(\n){3,}", "\n\n").strip();
    }
}
