package lk.ijse.examsy.gradingservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ijse.examsy.gradingservice.service.GroqGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqGradingServiceImpl implements GroqGradingService {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> evaluateAnswer(String questionText, String modelAnswer, String studentOcrText) {
        String systemPrompt = """
                You are an expert, strict but fair exam grader.
                
                You are grading answers extracted from handwritten exam papers using OCR.
                The OCR text may contain:
                - Spelling mistakes (e.g., "rnass" instead of "mass")
                - Missing punctuation
                - Broken or merged words
                - Random noise characters
                
                You MUST intelligently interpret the student's intent.
                
                GRADING RULES:
                1. Compare the student's answer with the model answer concept-by-concept.
                2. Identify key points from the model answer.
                3. Check which points are:
                   - Fully correct
                   - Partially correct
                   - Missing
                   - Incorrect
                4. Be strict but fair:
                   - Award partial marks where appropriate
                   - Do NOT give full marks unless the answer is conceptually complete
                5. Ignore minor OCR-related spelling mistakes if meaning is clear.
                
                OUTPUT REQUIREMENTS:
                - Return ONLY valid JSON (no markdown, no explanation outside JSON)
                - Follow this EXACT structure:
                {
                  "suggestedScore": <integer 0 to 100>,
                  "matchedConcepts": ["..."],
                  "missingConcepts": ["..."],
                  "incorrectParts": ["..."],
                  "comments": "<clear, short feedback>",
                  "confidence": "<High | Medium | Low>"
                }
                
                SCORING:
                - Base score on percentage of correct concepts covered
                - Penalize missing key points heavily
                - Penalize incorrect explanations moderately
                - Reward clarity and completeness
                
                IMPORTANT:
                Do NOT hallucinate content.
                Do NOT include anything outside the JSON.
                """;

        String userPrompt = String.format("""
                Question:
                %s
                
                Model Answer:
                %s
                
                Student Answer (OCR Extracted):
                %s
                """, questionText, modelAnswer, studentOcrText);

        try {
            if (groqApiKey == null || groqApiKey.isBlank()) {
                log.warn("Groq API key is not configured. Falling back to default grading response.");
                return fallbackResponse("Groq API key not configured.");
            }

            String requestBody = """
                    {
                      "model": "llama-3.1-8b-instant",
                      "messages": [
                        { "role": "system", "content": %s },
                        { "role": "user", "content": %s }
                      ],
                      "temperature": 0.1,
                      "response_format": { "type": "json_object" }
                    }
                    """.formatted(
                    objectMapper.writeValueAsString(systemPrompt),
                    objectMapper.writeValueAsString(userPrompt)
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(groqApiUrl))
                    .header("Authorization", "Bearer " + groqApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                String aiResponseText = rootNode.path("choices").get(0).path("message").path("content").asText();
                return objectMapper.readValue(aiResponseText, Map.class);
            } else {
                log.error("Groq API Error status {}: {}", response.statusCode(), response.body());
                return fallbackResponse("API Error: " + response.statusCode());
            }

        } catch (Exception e) {
            log.error("Failed to communicate with Groq LLM: {}", e.getMessage(), e);
            return fallbackResponse(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> gradeShortAnswer(String questionText, String modelAnswer, String studentAnswer, BigDecimal maxPoints) {
        String prompt = String.format("""
                You are a highly strict, fair, and accurate exam grader.
                
                TASK:
                Evaluate the student's answer based ONLY on the teacher's model answer.
                
                INPUT:
                Question: %s
                Model Answer: %s
                Student Answer: %s
                Maximum Score: %s
                
                GRADING INSTRUCTIONS:
                1. Identify key points in the model answer.
                2. Compare the student answer against those key points.
                3. Award marks proportionally based on correct coverage.
                4. Do NOT give full marks unless all key points are covered correctly.
                5. Ignore minor grammar mistakes unless they affect meaning.
                6. Penalize missing, incorrect, or irrelevant information.
                7. Do NOT guess - base grading strictly on provided answers.
                
                SCORING RULES:
                - Score must be between 0 and %s
                - Score must be logical and consistent with explanation
                - Use decimal scoring if needed (e.g., 2.5)
                
                OUTPUT FORMAT (STRICT JSON ONLY):
                {
                  "awarded_score": number,
                  "feedback": "Clear, concise explanation mentioning correct points and missing points"
                }
                """, questionText, modelAnswer, studentAnswer, maxPoints.toString(), maxPoints);

        try {
            if (groqApiKey == null || groqApiKey.isBlank()) {
                log.warn("Groq API key not configured for short answer grading.");
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("awarded_score", BigDecimal.ZERO);
                fallback.put("feedback", "Manual review required: Groq API key not configured.");
                return fallback;
            }

            String requestBody = """
                    {
                      "model": "llama-3.1-8b-instant",
                      "messages": [
                        { "role": "user", "content": %s }
                      ],
                      "temperature": 0.1,
                      "response_format": { "type": "json_object" }
                    }
                    """.formatted(objectMapper.writeValueAsString(prompt));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(groqApiUrl))
                    .header("Authorization", "Bearer " + groqApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return objectMapper.readValue(content, Map.class);
            } else {
                log.error("Groq short answer grading error {}: {}", response.statusCode(), response.body());
                Map<String, Object> err = new HashMap<>();
                err.put("awarded_score", BigDecimal.ZERO);
                err.put("feedback", "AI Grading failed with status " + response.statusCode());
                return err;
            }
        } catch (Exception e) {
            log.error("AI Short Answer Grading failed: {}", e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("awarded_score", BigDecimal.ZERO);
            err.put("feedback", "AI Grading exception: " + e.getMessage());
            return err;
        }
    }

    private Map<String, Object> fallbackResponse(String errorDetails) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("suggestedScore", 0);
        fallback.put("matchedConcepts", List.of());
        fallback.put("missingConcepts", List.of());
        fallback.put("incorrectParts", List.of("System Notice: AI evaluation unavailable."));
        fallback.put("comments", "Manual review required. Details: " + errorDetails);
        fallback.put("confidence", "Low");
        return fallback;
    }
}
