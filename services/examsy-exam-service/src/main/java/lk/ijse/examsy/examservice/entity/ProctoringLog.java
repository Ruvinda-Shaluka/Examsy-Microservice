package lk.ijse.examsy.examservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "proctoring_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ExamSubmission examSubmission;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}
