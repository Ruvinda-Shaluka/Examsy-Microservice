package lk.ijse.examsy.examservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exam_submissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "student_username"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Exam exam;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_username", nullable = false, length = 50)
    private String studentUsername;

    @Column(name = "student_name", length = 100)
    private String studentName;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Builder.Default
    @Column(length = 30)
    private String status = "NOT_STARTED";

    @Builder.Default
    @Column(name = "proctoring_status", length = 30)
    private String proctoringStatus = "SECURE";

    @Builder.Default
    @Column(name = "suspicious_event_count")
    private Integer suspiciousEventCount = 0;

    @Builder.Default
    @Column(name = "total_time_away_seconds")
    private Integer totalTimeAwaySeconds = 0;

    @Column(name = "last_known_action", length = 100)
    private String lastKnownAction;

    @Column(name = "calculated_score", precision = 5, scale = 2)
    private BigDecimal calculatedScore;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "awarded_grade_letter", length = 2)
    private String awardedGradeLetter;

    @Column(name = "pdf_submission_url")
    private String pdfSubmissionUrl;

    @Column(name = "pdf_feedback", columnDefinition = "TEXT")
    private String pdfFeedback;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SubmissionAnswer> answers;

    @OneToMany(mappedBy = "examSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProctoringLog> proctoringLogs;
}
