package lk.ijse.examsy.gradingservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ijse.examsy.gradingservice.dto.MockExamResponseDTO;
import lk.ijse.examsy.gradingservice.dto.MockQuestionDTO;
import lk.ijse.examsy.gradingservice.entity.MockExam;
import lk.ijse.examsy.gradingservice.entity.MockQuestion;
import lk.ijse.examsy.gradingservice.repository.MockExamRepo;
import lk.ijse.examsy.gradingservice.service.GroqMockExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqMockExamServiceImpl implements GroqMockExamService {

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private final MockExamRepo mockExamRepo;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Transactional
    @Override
    public MockExamResponseDTO generateAndSaveExam(String username, String subject, String topic, String difficulty, int count) {
        log.info("Generating mock exam for user '{}', subject: '{}', topic: '{}', difficulty: '{}', count: {}",
                username, subject, topic, difficulty, count);

        int maxRetries = 3;
        JsonNode generatedData = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            generatedData = callGroq(subject, topic, difficulty, count);
            if (isValidExam(generatedData, count)) {
                break;
            }
            if (attempt == maxRetries - 1) {
                throw new RuntimeException("AI failed to generate valid exam structure after retries");
            }
        }

        MockExam exam = MockExam.builder()
                .studentUsername(username)
                .subject(subject)
                .topic(topic)
                .difficulty(difficulty)
                .build();

        List<MockQuestion> questionEntities = new ArrayList<>();
        for (JsonNode qNode : generatedData.path("questions")) {
            JsonNode optionsNode = qNode.path("options");
            MockQuestion q = MockQuestion.builder()
                    .mockExam(exam)
                    .questionText(qNode.path("questionText").asText())
                    .optionA(optionsNode.get(0).asText())
                    .optionB(optionsNode.get(1).asText())
                    .optionC(optionsNode.get(2).asText())
                    .optionD(optionsNode.get(3).asText())
                    .correctOptionIndex(qNode.path("correctOptionIndex").asInt())
                    .explanation(qNode.path("explanation").asText())
                    .build();
            questionEntities.add(q);
        }

        exam.setQuestions(questionEntities);
        MockExam savedExam = mockExamRepo.save(exam);

        List<MockQuestionDTO> questionDTOs = savedExam.getQuestions().stream()
                .map(q -> MockQuestionDTO.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .options(List.of(q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                        .correctOptionIndex(q.getCorrectOptionIndex())
                        .explanation(q.getExplanation())
                        .build())
                .collect(Collectors.toList());

        return MockExamResponseDTO.builder()
                .id(savedExam.getId())
                .studentUsername(savedExam.getStudentUsername())
                .subject(savedExam.getSubject())
                .topic(savedExam.getTopic())
                .difficulty(savedExam.getDifficulty())
                .generatedAt(savedExam.getGeneratedAt())
                .questions(questionDTOs)
                .build();
    }

    private JsonNode callGroq(String subject, String topic, String difficulty, int count) {
        long seed = System.currentTimeMillis();

        String prompt = String.format("""
                Generate a mock exam in STRICT JSON format.
                
                RULES:
                - All answers MUST be 100%% correct.
                - Double-check every calculation.
                - Do NOT guess answers.
                - Ensure explanation matches the correct answer.
                - If math is involved, recompute carefully.
                - Do NOT produce invalid or inconsistent answers.
                
                Self-check before returning:
                1. Verify correct option is truly correct
                2. Verify explanation matches answer
                3. Fix any errors before output
                
                Subject: %s
                Topic: %s
                Difficulty: %s
                Number of Questions: %d
                Seed: %d
                
                Output ONLY valid JSON:
                {
                  "questions": [
                    {
                      "questionText": "...",
                      "options": ["A", "B", "C", "D"],
                      "correctOptionIndex": 0,
                      "explanation": "..."
                    }
                  ]
                }
                """, subject, topic, difficulty, count, seed);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String response = restTemplate.postForObject(groqApiUrl, request, String.class);
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readTree(content);
        } catch (Exception e) {
            log.error("Groq Mock Exam API error: {}", e.getMessage());
            throw new RuntimeException("Groq API error: " + e.getMessage());
        }
    }

    private boolean isValidExam(JsonNode data, int expectedCount) {
        if (data == null || !data.has("questions")) return false;

        JsonNode questions = data.get("questions");
        if (!questions.isArray() || questions.size() != expectedCount) return false;

        for (JsonNode q : questions) {
            if (!q.has("questionText") || !q.has("options") || !q.has("correctOptionIndex") || !q.has("explanation")) {
                return false;
            }
            if (q.get("options").size() != 4) return false;
            int correctIndex = q.get("correctOptionIndex").asInt();
            if (correctIndex < 0 || correctIndex > 3) return false;
        }

        return true;
    }
}
