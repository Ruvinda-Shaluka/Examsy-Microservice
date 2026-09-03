package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentExamViewDTO {
    private Integer examId;
    private String title;
    private String examType;
    private String examMode;
    private Integer durationMinutes;
    private LocalDateTime deadlineTime;
    private String pdfResourceUrl;
    private Integer submissionId;
    private String submissionStatus;
    private List<StudentQuestionDTO> questions;
}
