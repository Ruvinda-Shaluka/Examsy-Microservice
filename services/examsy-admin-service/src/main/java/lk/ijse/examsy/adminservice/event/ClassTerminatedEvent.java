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
public class ClassTerminatedEvent implements Serializable {
    private Integer reportId;
    private Integer courseId;
    private String courseName;
    private String teacherUsername;
    private String reporterUsername;
    private String reason;
    private String adminUsername;
    private LocalDateTime terminatedAt;
}
