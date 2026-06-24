package com.newsense.backend.ai.article;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsense.backend.ai.config.OpenAiProperties;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiArticleEvaluatorClient {

    private static final int MAX_ARTICLE_LENGTH = 12_000;
    private static final String GUIDELINE_PATH = "guidelines/importance_criteria.md";

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public ArticleImportanceEvaluation evaluate(String title, String articleText) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "messages", List.of(
                        Map.of("role", "developer", "content", createInstructions()),
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
            int score = output.path("importanceScore").asInt();
            return new ArticleImportanceEvaluation(
                    output.path("isImportant").asBoolean(),
                    Math.max(0, Math.min(100, score)),
                    output.path("reason").asText("")
            );
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.error("AI article importance evaluation failed: {}", exception.getMessage());
            throw new CustomException(ErrorCode.AI_CLASSIFICATION_FAILED);
        }
    }

    private String createInstructions() {
        return """
                당신은 경제 학습 서비스의 뉴스 선별 평가자입니다.
                아래 기준을 근거로 기사 저장 가치와 중요도를 평가하세요.
                기준에 없는 내용을 추측하지 말고 제목과 본문 근거만 사용하세요.

                """ + readGuidelines();
    }

    private String readGuidelines() {
        try {
            return new ClassPathResource(GUIDELINE_PATH)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read importance criteria", exception);
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

    private Map<String, Object> createResponseFormat() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("isImportant", Map.of("type", "boolean"));
        properties.put("importanceScore", Map.of("type", "integer", "minimum", 0, "maximum", 100));
        properties.put("reason", Map.of("type", "string"));

        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", properties,
                "required", List.of("isImportant", "importanceScore", "reason")
        );

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "article_importance_evaluation",
                        "strict", true,
                        "schema", schema
                )
        );
    }
}
