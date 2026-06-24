package com.newsense.backend.ai.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsense.backend.ai.config.AiPipelineProperties;
import com.newsense.backend.ai.config.OpenAiProperties;
import com.newsense.backend.quiz.domain.QuizType;
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
public class QuizCriticClient {

    private static final int MAX_ARTICLE_LENGTH = 8_000;
    private static final String INSTRUCTIONS = """
            당신은 Newsense 경제 퀴즈 검증자입니다.
            생성된 퀴즈 3문항이 기사 근거와 교육 목적에 맞는지 검증하세요.
            반드시 확인할 항목:
            1. 정확히 OX 1문항, 객관식 2문항인지
            2. 각 문항의 정답이 하나만 존재하는지
            3. 정답과 해설이 기사 본문 또는 근거 청크로 설명 가능한지
            4. 객관식 선택지가 중복 정답처럼 보이지 않는지
            5. 1번은 개념 이해, 2번은 사실 확인, 3번은 인과 추론 역할을 하는지
            6. 투자 조언이나 본문에 없는 전망이 섞이지 않았는지
            문제가 없으면 approved=true와 빈 issues를 반환하세요.
            """;

    private final RestClient openAiRestClient;
    private final OpenAiProperties openAiProperties;
    private final AiPipelineProperties pipelineProperties;
    private final ObjectMapper objectMapper;

    public QuizCritiqueResult critique(
            String title,
            String articleText,
            List<String> evidenceChunks,
            List<GeneratedQuiz> quizzes
    ) {
        QuizCritiqueResult localResult = localValidate(quizzes);
        if (!localResult.approved()) {
            return localResult;
        }
        if (openAiProperties.apiKey() == null || openAiProperties.apiKey().isBlank()) {
            return QuizCritiqueResult.approvedResult();
        }

        try {
            Map<String, Object> request = Map.of(
                    "model", pipelineProperties.quizCriticModel(openAiProperties.model()),
                    "messages", List.of(
                            Map.of("role", "developer", "content", INSTRUCTIONS),
                            Map.of("role", "user", "content", createInput(title, articleText, evidenceChunks, quizzes))
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
            return new QuizCritiqueResult(
                    output.path("approved").asBoolean(false),
                    toTextList(output.path("issues"))
            );
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.warn("AI quiz critique failed. Using local validation result: {}", exception.getMessage());
            return localResult;
        }
    }

    private QuizCritiqueResult localValidate(List<GeneratedQuiz> quizzes) {
        java.util.ArrayList<String> issues = new java.util.ArrayList<>();
        if (quizzes == null || quizzes.size() != 3) {
            issues.add("퀴즈는 정확히 3문항이어야 합니다.");
            return new QuizCritiqueResult(false, issues);
        }
        long oxCount = quizzes.stream().filter(quiz -> quiz.type() == QuizType.OX).count();
        long multipleCount = quizzes.stream().filter(quiz -> quiz.type() == QuizType.MULTIPLE).count();
        if (oxCount != 1 || multipleCount != 2) {
            issues.add("OX 1문항과 객관식 2문항 구성이 아닙니다.");
        }
        for (GeneratedQuiz quiz : quizzes) {
            if (quiz.question() == null || quiz.question().isBlank()) {
                issues.add("질문이 비어 있는 문항이 있습니다.");
            }
            if (quiz.explanation() == null || quiz.explanation().isBlank()) {
                issues.add("해설이 비어 있는 문항이 있습니다.");
            }
            if (quiz.options() == null || !quiz.options().contains(quiz.correctAnswer())) {
                issues.add("정답이 선택지에 포함되지 않은 문항이 있습니다.");
            }
            if (quiz.options() != null && quiz.options().stream().distinct().count() != quiz.options().size()) {
                issues.add("중복 선택지가 포함된 문항이 있습니다.");
            }
        }
        return new QuizCritiqueResult(issues.isEmpty(), List.copyOf(issues));
    }

    private String createInput(
            String title,
            String articleText,
            List<String> evidenceChunks,
            List<GeneratedQuiz> quizzes
    ) throws JsonProcessingException {
        return "기사 제목: " + title
                + "\n\n근거 청크:\n" + formatEvidenceChunks(evidenceChunks)
                + "\n\n기사 본문:\n" + truncate(articleText)
                + "\n\n생성된 퀴즈 JSON:\n" + objectMapper.writeValueAsString(quizzes);
    }

    private String truncate(String articleText) {
        if (articleText == null || articleText.isBlank()) {
            return "";
        }
        return articleText.length() <= MAX_ARTICLE_LENGTH
                ? articleText
                : articleText.substring(0, MAX_ARTICLE_LENGTH);
    }

    private String formatEvidenceChunks(List<String> evidenceChunks) {
        if (evidenceChunks == null || evidenceChunks.isEmpty()) {
            return "검색된 근거 청크 없음";
        }
        return evidenceChunks.stream()
                .limit(5)
                .map(chunk -> "- " + chunk)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("검색된 근거 청크 없음");
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
        properties.put("approved", Map.of("type", "boolean"));
        properties.put("issues", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "maxItems", 8
        ));
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("properties", properties);
        schema.put("required", List.of("approved", "issues"));

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "quiz_critique",
                        "strict", true,
                        "schema", schema
                )
        );
    }
}
