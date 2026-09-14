package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultExamItemDTO {
    private Integer id;
    private Integer examId;
    private String title;
    private String examType;
    private String examMode;
    private Integer durationMinutes;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime deadlineTime;
    private String status;
    private String studentStatus;

    private BigDecimal score;
    private BigDecimal maxScore;
    private String gradeLetter;
    private LocalDateTime completedAt;
    private String feedback;
}

