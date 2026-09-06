package lk.ijse.examsy.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "instructor_id", unique = true, length = 50)
    private String instructorId;

    @Column(length = 100)
    private String specialization;

    @Column(name = "office_location", length = 100)
    private String officeLocation;

    @Column(name = "professional_bio", columnDefinition = "TEXT")
    private String professionalBio;

    @Builder.Default
    @Column(name = "notify_email")
    private Boolean notifyEmail = true;

    @Builder.Default
    @Column(name = "notify_push")
    private Boolean notifyPush = true;

    @Builder.Default
    @Column(name = "notify_security")
    private Boolean notifySecurity = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
