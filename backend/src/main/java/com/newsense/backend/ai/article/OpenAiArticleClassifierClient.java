package com.newsense.backend.ai.article;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String INSTRUCTIONS = """
            당신은 한국 청년층을 위한 경제 뉴스 큐레이터입니다.
            제공된 기사 제목과 본문만 근거로 기사 메타데이터를 분류하세요.
            category는 반드시 거시경제, 금융/투자, 정책/제도, 기업/산업, 글로벌경제 중 하나입니다.
            difficulty는 반드시 초급, 중급, 고급 중 하나입니다.
            summary는 핵심 내용을 한국어 3줄로 요약하고, 각 줄은 과장 없이 기사 근거에 기반해야 합니다.
            related_stocks는 기사에 명시적으로 언급된 한국 상장 종목을 최대 3개까지 추출합니다.
            상장 종목이 없거나 불확실하면 빈 배열을 반환하세요.
            """;

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public ArticleClassificationResult classify(String title, String articleText) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "messages", List.of(
                        Map.of("role", "developer", "content", INSTRUCTIONS),
                        Map.of("role", "user", "content", createInput(title, articleText))
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
            return new ArticleClassificationResult(category, difficulty, summary.trim(), relatedStocks);
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.error("AI article classification failed: {}", exception.getMessage());
            throw new CustomException(ErrorCode.AI_CLASSIFICATION_FAILED);
        }
    }

    private String createInput(String title, String articleText) {
        return "기사 제목: " + title + "\n\n기사 본문:\n" + truncate(articleText);
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

    private List<RelatedStockInfo> parseRelatedStocks(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<RelatedStockInfo> result = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            String name = item.path("stockName").asText("");
            String code = item.path("stockCode").asText("");
            String reason = item.path("relationReason").asText("");
            if (!name.isBlank() && !code.isBlank()) {
                result.add(new RelatedStockInfo(name, code, reason));
            }
        }
        return result;
    }

    private Map<String, Object> createResponseFormat() {
        Map<String, Object> stockItemProps = new LinkedHashMap<>();
        stockItemProps.put("stockName", Map.of("type", "string"));
        stockItemProps.put("stockCode", Map.of("type", "string"));
        stockItemProps.put("relationReason", Map.of("type", "string"));

        Map<String, Object> stockItemSchema = new LinkedHashMap<>();
        stockItemSchema.put("type", "object");
        stockItemSchema.put("additionalProperties", false);
        stockItemSchema.put("properties", stockItemProps);
        stockItemSchema.put("required", List.of("stockName", "stockCode", "relationReason"));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("category", Map.of(
                "type", "string",
                "enum", List.of("거시경제", "금융/투자", "정책/제도", "기업/산업", "글로벌경제")
        ));
        properties.put("difficulty", Map.of(
                "type", "string",
                "enum", List.of("초급", "중급", "고급")
        ));
        properties.put("summary", Map.of("type", "string"));
        properties.put("related_stocks", Map.of(
                "type", "array",
                "items", stockItemSchema
        ));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("properties", properties);
        schema.put("required", List.of("category", "difficulty", "summary", "related_stocks"));

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "article_classification",
                        "strict", true,
                        "schema", schema
                )
        );
    }
}
