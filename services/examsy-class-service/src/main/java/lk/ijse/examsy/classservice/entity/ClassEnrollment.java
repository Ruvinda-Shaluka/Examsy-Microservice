package lk.ijse.examsy.classservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_id", "student_username"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Course course;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_username", nullable = false, length = 50)
    private String studentUsername;

    @Column(name = "student_name", nullable = false, length = 100)
    private String studentName;

    @Column(name = "student_email", length = 100)
    private String studentEmail;

    @Column(name = "student_profile_picture_url")
    private String studentProfilePictureUrl;

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;
}
