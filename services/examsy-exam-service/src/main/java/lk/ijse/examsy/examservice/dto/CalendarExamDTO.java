package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarExamDTO {
    private Integer id;
    private Integer classId;
    private String title;
    private String courseName;
    private String themeColorHex;
    private LocalDateTime examDate;
    private String examMode;
}
