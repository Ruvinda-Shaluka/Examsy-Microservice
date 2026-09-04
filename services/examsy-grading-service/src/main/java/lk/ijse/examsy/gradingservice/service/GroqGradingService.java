package lk.ijse.examsy.gradingservice.service;

import java.math.BigDecimal;
import java.util.Map;

public interface GroqGradingService {

    Map<String, Object> evaluateAnswer(String questionText, String modelAnswer, String studentOcrText);

    Map<String, Object> gradeShortAnswer(String questionText, String modelAnswer, String studentAnswer, BigDecimal maxPoints);
}
