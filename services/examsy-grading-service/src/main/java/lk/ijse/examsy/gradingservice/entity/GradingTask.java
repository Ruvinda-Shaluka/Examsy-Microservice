package lk.ijse.examsy.gradingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grading_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "submission_id", nullable = false)
    private Integer submissionId;

    @Column(name = "exam_id", nullable = false)
    private Integer examId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_username", nullable = false, length = 100)
    private String studentUsername;

    @Column(name = "teacher_username", length = 100)
    private String teacherUsername;

    @Column(name = "exam_type", length = 50)
    private String examType;

    @Column(name = "pdf_submission_url", length = 500)
    private String pdfSubmissionUrl;

    @Column(name = "extracted_ocr_text", columnDefinition = "LONGTEXT")
    private String extractedOcrText;

    @Column(name = "suggested_score", precision = 5, scale = 2)
    private BigDecimal suggestedScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "awarded_grade_letter", length = 5)
    private String awardedGradeLetter;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING_OCR";

    @Column(name = "matched_concepts", columnDefinition = "TEXT")
    private String matchedConcepts;

    @Column(name = "missing_concepts", columnDefinition = "TEXT")
    private String missingConcepts;

    @Column(name = "incorrect_parts", columnDefinition = "TEXT")
    private String incorrectParts;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(length = 20)
    private String confidence;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
