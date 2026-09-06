package lk.ijse.examsy.gradingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockExamRequestDTO {
    private String subject;
    private String topic;
    private String difficulty;
    private int count;
}
