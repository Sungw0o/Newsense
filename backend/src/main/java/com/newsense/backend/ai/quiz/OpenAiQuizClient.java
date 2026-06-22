package com.newsense.backend.ai.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsense.backend.ai.config.OpenAiProperties;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
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
public class OpenAiQuizClient {

    private static final int MAX_ARTICLE_LENGTH = 12_000;
    private static final String INSTRUCTIONS = """
            당신은 한국 청년층의 경제 문해력을 돕는 교육용 퀴즈 출제자입니다.
            제공된 기사에 명시된 사실만 사용해 한국어 퀴즈 3문항을 만드세요.
            정확히 OX 1문항과 객관식(MULTIPLE) 2문항을 작성하세요.
            OX 선택지는 반드시 [\"O\", \"X\"]이고 정답도 O 또는 X여야 합니다.
            객관식은 서로 중복되지 않는 선택지 4개를 제공하고 정답은 선택지 중 하나여야 합니다.
            질문은 모호하지 않게, 해설은 기사 근거를 짧고 명확하게 설명하세요.
            기사에 근거가 부족하면 추측하지 말고 기사에서 직접 확인 가능한 내용으로 출제하세요.
            """;

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public List<GeneratedQuiz> generate(
            String title,
            String articleText,
            List<EconomicTermContext> economicTerms
    ) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new CustomException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        String input = "기사 제목: " + title
                + "\n\n기사 본문:\n" + truncate(articleText)
                + "\n\n기재부 경제 용어 사전:\n" + formatTerms(economicTerms);
        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "instructions", INSTRUCTIONS,
                "input", input,
                "text", Map.of("format", createResponseFormat())
        );

        try {
            JsonNode response = openAiRestClient.post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            GeneratedQuizSet generated = objectMapper.readValue(extractOutputText(response), GeneratedQuizSet.class);
            validate(generated.quizzes());
            return generated.quizzes();
        } catch (RestClientException | JsonProcessingException | IllegalArgumentException exception) {
            log.error("OpenAI quiz generation failed: {}", exception.getMessage());
            throw new CustomException(ErrorCode.QUIZ_GENERATION_FAILED);
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            throw new IllegalArgumentException("OpenAI response is empty");
        }
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    String text = content.path("text").asText();
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }
        }
        throw new IllegalArgumentException("OpenAI response has no output text");
    }

    private void validate(List<GeneratedQuiz> quizzes) {
        if (quizzes == null || quizzes.size() != 3) {
            throw new IllegalArgumentException("Exactly three quizzes are required");
        }

        long oxCount = quizzes.stream().filter(quiz -> quiz.type() == QuizType.OX).count();
        long multipleCount = quizzes.stream().filter(quiz -> quiz.type() == QuizType.MULTIPLE).count();
        if (oxCount != 1 || multipleCount != 2) {
            throw new IllegalArgumentException("One OX and two multiple-choice quizzes are required");
        }

        for (GeneratedQuiz quiz : quizzes) {
            if (quiz.question() == null || quiz.question().isBlank()
                    || quiz.explanation() == null || quiz.explanation().isBlank()
                    || quiz.options() == null || !quiz.options().contains(quiz.correctAnswer())) {
                throw new IllegalArgumentException("Generated quiz is invalid");
            }
            if (quiz.type() == QuizType.OX && !quiz.options().equals(List.of("O", "X"))) {
                throw new IllegalArgumentException("OX options must be O and X");
            }
            if (quiz.type() == QuizType.MULTIPLE && quiz.options().size() != 4) {
                throw new IllegalArgumentException("Multiple-choice quiz must have four options");
            }
        }
    }

    private String truncate(String articleText) {
        if (articleText == null || articleText.isBlank()) {
            throw new IllegalArgumentException("Article text is empty");
        }
        return articleText.length() <= MAX_ARTICLE_LENGTH
                ? articleText
                : articleText.substring(0, MAX_ARTICLE_LENGTH);
    }

    private String formatTerms(List<EconomicTermContext> economicTerms) {
        if (economicTerms == null || economicTerms.isEmpty()) {
            return "기사에서 매칭된 경제 용어 없음";
        }
        return economicTerms.stream()
                .limit(20)
                .map(term -> "- " + term.name() + ": " + term.definition())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("기사에서 매칭된 경제 용어 없음");
    }

    private Map<String, Object> createResponseFormat() {
        Map<String, Object> quizProperties = new LinkedHashMap<>();
        quizProperties.put("type", Map.of("type", "string", "enum", List.of("OX", "MULTIPLE")));
        quizProperties.put("question", Map.of("type", "string"));
        quizProperties.put("options", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "minItems", 2,
                "maxItems", 4
        ));
        quizProperties.put("correctAnswer", Map.of("type", "string"));
        quizProperties.put("explanation", Map.of("type", "string"));

        Map<String, Object> quizSchema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", quizProperties,
                "required", List.of("type", "question", "options", "correctAnswer", "explanation")
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of("quizzes", Map.of(
                        "type", "array",
                        "items", quizSchema,
                        "minItems", 3,
                        "maxItems", 3
                )),
                "required", List.of("quizzes")
        );

        return Map.of(
                "type", "json_schema",
                "name", "article_quizzes",
                "strict", true,
                "schema", schema
        );
    }
}
