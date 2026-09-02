package lk.ijse.examsy.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent {
    private Integer userId;
    private String username;
    private String email;
    private String role; // "STUDENT" or "TEACHER"
    private String fullName;

    // Student specific fields
    private String studentIdentificationNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String grade;

    // Teacher specific fields
    private String instructorId;
    private String specialization;

    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();
}
