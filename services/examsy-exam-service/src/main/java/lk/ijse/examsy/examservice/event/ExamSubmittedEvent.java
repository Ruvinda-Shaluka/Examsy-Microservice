package lk.ijse.examsy.examservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Kafka event dispatched to topic 'examsy.exam.submitted'.
 * Consumed by Phase 9 (examsy-grading-service) for automated OCR and AI Groq evaluation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmittedEvent implements Serializable {
    private Integer submissionId;
    private Integer examId;
    private Integer studentId;
    private String studentUsername;
    private String examType;
    private String pdfSubmissionUrl;
    private LocalDateTime submittedAt;
}
