package lk.ijse.examsy.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectAlertRequestDTO {
    @NotBlank
    private String targetUsername;
    private String targetEmail;
    @NotBlank
    private String title;
    @NotBlank
    private String message;
    private Integer courseId;
    @Builder.Default
    private Boolean sendEmail = false;
}
