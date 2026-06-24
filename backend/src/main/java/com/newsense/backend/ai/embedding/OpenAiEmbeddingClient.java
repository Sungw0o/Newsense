package com.newsense.backend.ai.embedding;

import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingClient {

    // text-embedding-3-large 최대 입력 토큰 약 8191 → 문자 기준 보수적으로 30,000자
    private static final int MAX_INPUT_CHARS = 30_000;

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final AiPipelineProperties pipelineProperties;
    private final ObjectMapper objectMapper;

    /**
     * 주어진 텍스트에 대한 임베딩 벡터를 반환합니다.
     * API 키 미설정 시 빈 리스트를 반환합니다.
     *
     * @param text 임베딩할 텍스트
     * @return 임베딩 벡터 (text-embedding-3-large: 3072차원)
     */
    public List<Double> embed(String text) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            log.warn("OpenAI API key not configured — skipping embedding generation");
            return List.of();
        }
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String input = text.length() > MAX_INPUT_CHARS ? text.substring(0, MAX_INPUT_CHARS) : text;
        Map<String, Object> requestBody = Map.of(
                "model", pipelineProperties.embeddingModel(),
                "input", input
        );

        try {
            String responseJson = openAiRestClient.post()
                    .uri("/embeddings")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode embeddingNode = root.path("data").path(0).path("embedding");

            if (embeddingNode.isMissingNode() || !embeddingNode.isArray()) {
                log.warn("Unexpected embedding response structure");
                return List.of();
            }

            List<Double> embedding = new ArrayList<>(embeddingNode.size());
            for (JsonNode val : embeddingNode) {
                embedding.add(val.doubleValue());
            }
            return embedding;
        } catch (RestClientException e) {
            log.warn("OpenAI embedding API call failed: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.warn("Failed to parse embedding response: {}", e.getMessage());
            return List.of();
        }
    }
}
