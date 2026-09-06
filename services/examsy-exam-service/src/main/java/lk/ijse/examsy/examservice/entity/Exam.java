package lk.ijse.examsy.examservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "teacher_username", nullable = false, length = 50)
    private String teacherUsername;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    @Column(name = "exam_mode", length = 20)
    private String examMode = "REAL_TIME";

    @Builder.Default
    @Column(name = "exam_type", length = 20)
    private String examType = "MCQ";

    @Column(name = "scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    @Column(name = "deadline_time")
    private LocalDateTime deadlineTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "pdf_resource_url")
    private String pdfResourceUrl;

    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Builder.Default
    @Column(length = 20)
    private String status = "PUBLISHED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Question> questions;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ExamSubmission> submissions;
}
