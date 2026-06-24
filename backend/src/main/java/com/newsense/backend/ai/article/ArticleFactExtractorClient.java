package com.newsense.backend.ai.article;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.config.OpenAiProperties;
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
public class ArticleFactExtractorClient {

    private static final int MAX_ARTICLE_LENGTH = 16_000;
    private static final String INSTRUCTIONS = """
            당신은 경제 기사 팩트 추출기입니다.
            기사 본문에서 요약과 퀴즈에 반드시 참고해야 할 근거만 추출하세요.
            quantitativeFacts에는 금리, 환율, 지수, 증감률, 예산, 기간 등 숫자가 포함된 사실을 넣으세요.
            keyTerms에는 기사 이해에 중요한 경제 명사와 제도명을 넣으세요.
            eventFacts에는 정책 발표, 시장 변화, 기업 사건 등 핵심 이벤트를 넣으세요.
            본문에 없는 추론이나 전망은 작성하지 마세요.
            """;

    private final RestClient openAiRestClient;
    private final OpenAiProperties openAiProperties;
    private final AiPipelineProperties pipelineProperties;
    private final ObjectMapper objectMapper;

    public ArticleFactExtractionResult extract(String title, String articleText) {
        if (openAiProperties.apiKey() == null || openAiProperties.apiKey().isBlank()) {
            return ArticleFactExtractionResult.empty();
        }

        try {
            Map<String, Object> request = Map.of(
                    "model", pipelineProperties.factExtractorModel(),
                    "messages", List.of(
                            Map.of("role", "developer", "content", INSTRUCTIONS),
                            Map.of("role", "user", "content", createInput(title, articleText))
                    ),
                    "response_format", createResponseFormat()
            );
            JsonNode response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProperties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode output = objectMapper.readTree(extractOutputText(response));
            return new ArticleFactExtractionResult(
                    toTextList(output.path("quantitativeFacts")),
                    toTextList(output.path("keyTerms")),
                    toTextList(output.path("eventFacts"))
            );
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.warn("Article fact extraction failed. Continuing without extracted facts: {}", exception.getMessage());
            return ArticleFactExtractionResult.empty();
        }
    }

    private String createInput(String title, String articleText) {
        return "기사 제목: " + title + "\n\n기사 본문:\n" + truncate(articleText);
    }

    private String truncate(String articleText) {
        if (articleText == null || articleText.isBlank()) {
            throw new IllegalArgumentException("Article text is empty");
        }
        return articleText.length() <= MAX_ARTICLE_LENGTH
                ? articleText
                : articleText.substring(0, MAX_ARTICLE_LENGTH);
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

    private List<String> toTextList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        node.forEach(item -> {
            String value = item.asText("");
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        });
        return List.copyOf(values);
    }

    private Map<String, Object> createResponseFormat() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("quantitativeFacts", stringArraySchema());
        properties.put("keyTerms", stringArraySchema());
        properties.put("eventFacts", stringArraySchema());

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("properties", properties);
        schema.put("required", List.of("quantitativeFacts", "keyTerms", "eventFacts"));

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "article_fact_extraction",
                        "strict", true,
                        "schema", schema
                )
        );
    }

    private Map<String, Object> stringArraySchema() {
        return Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "maxItems", 8
        );
    }
}
