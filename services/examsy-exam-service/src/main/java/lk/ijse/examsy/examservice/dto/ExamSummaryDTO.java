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
public class ExamSummaryDTO {
    private Integer id;
    private String title;
    private String examType;
    private String examMode;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime deadlineTime;
    private Integer durationMinutes;
    private BigDecimal maxScore;
    private String status;
    private Integer totalSubmissions;
}
