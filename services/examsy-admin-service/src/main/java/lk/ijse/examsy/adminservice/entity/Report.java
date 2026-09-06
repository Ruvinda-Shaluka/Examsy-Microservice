package lk.ijse.examsy.adminservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reporter_student_id")
    private Integer reporterStudentId;

    @Column(name = "reporter_student_username", nullable = false, length = 100)
    private String reporterStudentUsername;

    @Column(name = "reporter_student_name", length = 150)
    private String reporterStudentName;

    @Column(name = "target_course_id", nullable = false)
    private Integer targetCourseId;

    @Column(name = "target_course_name", nullable = false, length = 150)
    private String targetCourseName;

    @Column(name = "target_teacher_id")
    private Integer targetTeacherId;

    @Column(name = "target_teacher_username", length = 100)
    private String targetTeacherUsername;

    @Column(name = "target_teacher_name", length = 150)
    private String targetTeacherName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "priority_level", nullable = false, length = 20)
    private String priorityLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(length = 30)
    private String status = "PENDING";

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "reported_at", updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by_admin_username", length = 100)
    private String resolvedByAdminUsername;
}
