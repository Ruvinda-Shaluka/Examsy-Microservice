package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubmitDTO {
    private List<AnswerSubmitDTO> answers;
    private String pdfSubmissionUrl;
}
