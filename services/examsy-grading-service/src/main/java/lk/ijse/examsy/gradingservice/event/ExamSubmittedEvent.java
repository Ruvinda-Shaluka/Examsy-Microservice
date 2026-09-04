package lk.ijse.examsy.gradingservice.event;

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
public class ExamSubmittedEvent implements Serializable {
    private Integer submissionId;
    private Integer examId;
    private Integer studentId;
    private String studentUsername;
    private String examType;
    private String pdfSubmissionUrl;
    private LocalDateTime submittedAt;
}
