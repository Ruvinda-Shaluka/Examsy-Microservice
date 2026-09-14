package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OngoingExamDTO {
    private Integer id;
    private String title;
    private String className;
    private String examMode;
    private Integer activeStudents;
    private Integer submissions;
    private Integer totalStudents;
    private String remainingTime;
    private String deadline;
}
