package lk.ijse.examsy.examservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOptionDTO {
    private Integer id;
    private String optionText;
    // Note: isCorrect is deliberately excluded to prevent client-side inspection / cheating!
}
