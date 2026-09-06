package lk.ijse.examsy.gradingservice.service;

import lk.ijse.examsy.gradingservice.dto.MockExamResponseDTO;

public interface GroqMockExamService {

    MockExamResponseDTO generateAndSaveExam(String username, String subject, String topic, String difficulty, int count);
}
