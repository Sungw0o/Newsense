package com.newsense.backend.article.crawler.util;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ContentCleaner {

    /*
     * Scope:
     * - Implemented: HTML tag removal, whitespace normalization, reporter email removal,
     *   copyright/redistribution notice removal, and common byline/source signature removal.
     * - Out of current implementation: broad advertising copy, repeated footer blocks, and
     *   publisher-specific metadata. Those rules need source-specific patterns to avoid
     *   deleting valid article sentences.
     */

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

    // \uAE30\uC0AC \uB9D0\uBBF8 \uC778\uC0AC\uB9D0 \u00B7 \uAD11\uACE0\uC131 \uBB38\uAD6C \uD328\uD134
    private static final Pattern TAIL_GREET = Pattern.compile(
            "^.{0,80}(?:\uAD6C\uB3C5|\uD314\uB85C\uC6B0|\uC751\uC6D0|\uD074\uB9AD|\uC88B\uC544\uC694|\uACF5\uC720|\uC81C\uBCF4|\uC81C\uBCF4\uCC98|\uC5F0\uB77D\uCC98|\uB3C5\uC790|\uD6C4\uC6D0|\uAD11\uACE0|\uC2A4\uD3F0\uC11C|\uC81C\uACF5|\uBC14\uB85C\uAC00\uAE30|\uB354\uBCF4\uAE30|\uAD00\uB828\uAE30\uC0AC|\uAD00\uB828 \uAE30\uC0AC|[Ss]ubscribe|[Ff]ollow us).{0,80}$",
            Pattern.MULTILINE);

    // \uC804\uD654\uBC88\uD638 \uD328\uD134 (\uAF2C\uB9AC\uB9D0 \uC5F0\uB77D\uCC98)
    private static final Pattern PHONE = Pattern.compile(
            "(?:^|\\s)(?:02|0[3-9][0-9]|010|011|016|017|018|019)[\\s\\-]?\\d{3,4}[\\s\\-]?\\d{4}(?=\\s|$)");

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

        // \uAE30\uC7