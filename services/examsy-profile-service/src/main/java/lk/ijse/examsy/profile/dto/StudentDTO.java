package lk.ijse.examsy.profile.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    @Positive(message = "ID must be a valid positive integer")
    private Integer id;

    private Integer userAccountId;
    private String username;
    private String email;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    private String profilePictureUrl;

    @Size(max = 50, message = "Student ID number cannot exceed 50 characters")
    private String studentIdentificationNumber;

    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender cannot exceed 20 characters")
    private String gender;

    @Size(max = 20, message = "Grade cannot exceed 20 characters")
    private String grade;

    @Size(max = 100, message = "Major cannot exceed 100 characters")
    private String major;

    @Size(max = 500, message = "Academic bio cannot exceed 500 characters")
    private String academicBio;

    @PositiveOrZero(message = "Cumulative GPA cannot be negative")
    private BigDecimal cumulativeGpa;

    private Boolean notifyEmail;
    private Boolean notifyPush;
    private Boolean notifyIdentity;
}
