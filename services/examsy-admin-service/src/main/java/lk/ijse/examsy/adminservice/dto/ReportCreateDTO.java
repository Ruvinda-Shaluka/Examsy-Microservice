package lk.ijse.examsy.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCreateDTO {

    @NotNull(message = "Target course ID is required")
    private Integer targetCourseId;

    private String targetCourseName;

    private Integer targetTeacherId;

    private String targetTeacherUsername;

    private String targetTeacherName;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Priority level is required")
    private String priorityLevel;
}
