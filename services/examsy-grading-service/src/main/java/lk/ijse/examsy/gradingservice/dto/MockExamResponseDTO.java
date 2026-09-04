package lk.ijse.examsy.gradingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockExamResponseDTO {
    private Integer id;
    private String studentUsername;
    private String subject;
    private String topic;
    private String difficulty;
    private LocalDateTime generatedAt;
    private List<MockQuestionDTO> questions;
}
