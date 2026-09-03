package lk.ijse.examsy.classservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAnnouncementDTO {

    @NotBlank(message = "Content cannot be empty.")
    private String content;
}
