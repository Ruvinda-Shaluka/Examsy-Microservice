package lk.ijse.examsy.adminservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassReportedEvent implements Serializable {
    private Integer reporterStudentId;
    private String reporterStudentUsername;
    private String reporterStudentName;
    private Integer targetCourseId;
    private String targetCourseName;
    private Integer targetTeacherId;
    private String targetTeacherUsername;
    private String targetTeacherName;
    private String category;
    private String priorityLevel;
    private String description;
    private LocalDateTime reportedAt;
}
