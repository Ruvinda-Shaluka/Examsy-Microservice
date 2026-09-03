package lk.ijse.examsy.classservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "teacher_username", nullable = false, length = 50)
    private String teacherUsername;

    @Column(name = "teacher_name", nullable = false, length = 100)
    private String teacherName;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "section_name", length = 50)
    private String sectionName;

    @Column(name = "academic_term", length = 50)
    private String academicTerm;

    @Column(name = "class_code", nullable = false, unique = true, length = 20)
    private String classCode;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @Builder.Default
    @Column(name = "theme_color_hex", length = 7)
    private String themeColorHex = "#4F46E5";

    @Builder.Default
    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "class_code_updated_at")
    private LocalDateTime classCodeUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ClassEnrollment> enrollments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ClassJoinRequest> joinRequests;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ClassAnnouncement> announcements;
}
