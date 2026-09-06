package lk.ijse.examsy.profile.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDTO {

    @Positive(message = "ID must be a valid positive integer")
    private Integer id;

    private Integer userAccountId;
    private String username;
    private String email;

    @Size(max = 100, message = "Full name cannot exceed 100 characters.")
    private String fullName;

    private String profilePictureUrl;

    @Size(max = 50, message = "Instructor ID cannot exceed 50 characters.")
    private String instructorId;

    @Size(max = 100, message = "Specialization cannot exceed 100 characters.")
    private String specialization;

    @Size(max = 100, message = "Office location cannot exceed 100 characters.")
    private String officeLocation;

    @Size(max = 500, message = "Professional bio cannot exceed 500 characters.")
    private String professionalBio;

    private Boolean notifyEmail;
    private Boolean notifyPush;
    private Boolean notifySecurity;
}
