package com.newsense.backend.ai.article;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.config.OpenAiProperties;
import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiArticleClassifierClient {

    private static final int MAX_ARTICLE_LENGTH = 12_000;
    private static final int SUMMARY_LINE_LIMIT = 3;
    private static final int SUMMARY_LINE_MAX_LENGTH = 80;
    private static final String INSTRUCTIONS = """
            당신은 한국 청년층을 위한 경제 뉴스 큐레이터입니다.
            제공된 기사 제목과 본문만 근거로 기사 메타데이터를 분류하세요.
            별도로 제공된 '선행 팩트 추출 결과'가 있으면 요약과 분류의 1차 근거로 사용하세요.
            category는 반드시 거시경제, 금융/투자, 정책/제도, 기업/산업, 글로벌경제 중 하나입니다.
            category 경계:
            - 거시경제: 금리, 물가, 성장률, 고용, GDP 등 경제 전반 지표
            - 금융/투자: 주식, 채권, 펀드, 금융상품, 금융기관, 투자시장
            - 정책/제도: 정부 정책, 법령, 규제, 지원제도, 세제 변화
            - 기업/산업: 특정 기업, 산업 구조, 실적, 공급망, 기술 변화
            - 글로벌경제: 해외 경제, 국제 금융, 주요국 정책, 환율, 무역
            difficulty는 반드시 초급, 중급, 고급 중 하나입니다.
            difficulty 경계:
            - 초급: 핵심 경제 개념이 1~2개이고 별도 배경지식이 거의 필요 없음
            - 중급: 여러 경제 지표나 정책 효과를 함께 이해해야 함
            - 고급: 복합 인과관계, 시장 파급효과, 제도 맥락, 수식 또는 전문 용어 밀도가 높음
            summary는 핵심 내용을 한국어 3줄로 요약하세요.
            summary에는 본문 속 중요한 정량 수치(금리, 환율, 지수, 증감률, 예산, 기간 등)를 최소 1개 포함하세요.
            "이 뉴스는 ~에 대한 내용입니다" 같은 상투적 표현은 금지합니다.
            각 줄은 주어, 목적어, 서술어가 분명하고 기사 근거에 기반해야 합니다.
            related_stocks는 기사에 명시적으로 언급된 한국 상장 종목을 최대 3개까지 추출합니다.
            상장 종목이 없거나 불확실하면 빈 배열을 반환하세요.
            """;

    private static final String SUMMARY_FORMAT_INSTRUCTIONS = """
            Summary format is mandatory:
            - Return summary as exactly 3 newline-separated Korean lines.
            - Line 1: event or change.
            - Line 2: the most important number, evidence, or factual basis.
            - Line 3: meaning or impact.
            - Each line must be 80 Korean characters or fewer.
            - Use only article-grounded facts. Do not include investment advice, hype, or speculation.
            - Do not prefix the lines with bullets, numbers, labels, or markdown.
            """;
    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final AiPipelineProperties pipelineProperties;
    private final ArticleFactExtractorClient factExtractorClient;
    private final ObjectMapper objectMapper;

    public ArticleClassificationResult classify(String title, String articleText) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        Map<String, Object> request = Map.of(
                "model", pipelineProperties.articleClassifierModel(properties.model()),
                "messages", List.of(
                        Map.of("role", "developer", "content", INSTRUCTIONS + "\n" + SUMMARY_FORMAT_INSTRUCTIONS),
                        Map.of("role", "user", "content", createInput(
                                title,
                                articleText,
                                factExtractorClient.extract(title, articleText)
                        ))
                ),
                "response_format", createResponseFormat()
        );

        try {
            JsonNode response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode output = objectMapper.readTree(extractOutputText(response));
            ArticleCategory category = ArticleCategory.from(output.path("category").asText());
            ArticleDifficulty difficulty = ArticleDifficulty.from(output.path("difficulty").asText());
            String summary = output.path("summary").asText();
            if (summary == null || summary.isBlank()) {
                throw new IllegalArgumentException("Summary is empty");
            }
            List<RelatedStockInfo> relatedStocks = parseRelatedStocks(output.path("related_stocks"));
            return new ArticleClassificationResult(category, difficulty, normalizeSummary(summary), relatedStocks);
        } catch (RestClientException | JacksonException | IllegalArgumentException exception) {
            log.error("AI article classification failed: {}", exception.getMessage());
            throw new CustomException(ErrorCode.AI_CLASSIFICATION_FAILED);
        }
    }

    private String createInput(String title, String articleText, ArticleFactExtractionResult facts) {
        return "기사 제목: " + title
                + "\n\n선행 팩트 추출 결과:\n" + formatFacts(facts)
                + "\n\n기사 본문:\n" + truncate(articleText);
    }

    private String formatFacts(ArticleFactExtractionResult facts) {
        if (facts == null || facts.isEmpty()) {
            return "추출된 선행 팩트 없음";
        }
        return "정량 수치:\n" + formatList(facts.quantitativeFacts())
                + "\n핵심 용어:\n" + formatList(facts.keyTerms())
                + "\n핵심 이벤트:\n" + formatList(facts.eventFacts());
    }

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "- 없음";
        }
        return values.stream()
                .limit(8)
                .map(value -> "- " + value)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- 없음");
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            throw new IllegalArgumentException("AI response is empty");
        }
        JsonNode content = response.path("choices").path(0).path("message").path("content");
        if (content.isTextual() && !content.asText().isBlank()) {
            return content.asText();
        }
        throw new IllegalArgumentException("AI response has no output text");
    }

    private String truncate(String articleText) {
        if (articleText == null || articleText.isBlank()) {
            throw new IllegalArgumentException("Article text is empty");
        }
        return articleText.length() <= MAX_ARTICLE_LENGTH
                ? articleText
                : articleText.substring(0, MAX_ARTICLE_LENGTH);
    }

    private String normalizeSummary(String summary) {
        List<String> lines = splitSummaryLines(summary);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Summary is empty");
        }
        return String.join("\n", lines.stream()
                .limit(SUMMARY_LINE_LIMIT)
                .map(this::trimSummaryLine)
                .toList());
    }

    private List<String> splitSummaryLines(String summary) {
        String normalized = summary == null ? "" : summary.trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        List<String> lineItems = java.util.Arrays.stream(normalized.split("\\R+"))
                .map(line -> line.replaceFirst("^\\s*\\d+[.)]\\s*", "").trim())
                .filter(line -> !line.isBlank())
                .toList();
        if (lineItems.size() > 1) {
            return lineItems;
        }

        return java.util.Arrays.stream(normalized.split("(?<=[.!?])\\s+"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private String trimSummaryLine(String line) {
        if (line.length() <= SUMMARY_LINE_MAX_LENGTH) {
            return line;
        }
        return line.substring(0, SUMMARY_LINE_MAX_LENGTH).trim();
    }

    private List<RelatedStockInfo> parseRelatedStocks(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<RelatedStockInfo> result = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            String name = item.path("stockName").asText("");
            String code = item.path("stockCode").asText("");
            String reason 