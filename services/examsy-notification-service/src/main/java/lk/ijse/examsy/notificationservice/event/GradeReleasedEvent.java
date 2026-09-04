package lk.ijse.examsy.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeReleasedEvent implements Serializable {
    private Integer submissionId;
    private Integer examId;
    private Integer studentId;
    private String studentUsername;
    private BigDecimal finalScore;
    private BigDecimal maxScore;
    private String awardedGradeLetter;
    private String feedback;
    private LocalDateTime releasedAt;
}
