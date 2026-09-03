package lk.ijse.examsy.classservice.controller;

import jakarta.validation.Valid;
import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.classservice.dto.ClassPeopleDTO;
import lk.ijse.examsy.classservice.dto.JoinClassDTO;
import lk.ijse.examsy.classservice.dto.StudentClassCardDTO;
import lk.ijse.examsy.classservice.service.StudentClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/dashboard")
@RequiredArgsConstructor
@Validated
public class StudentClassController {

    private final StudentClassService studentClassService;

    @GetMapping("/classes")
    public ResponseEntity<APIResponse<List<StudentClassCardDTO>>> getEnrolledClasses(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<StudentClassCardDTO> classes = studentClassService.getMyEnrolledClasses(userDetails.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Classes fetched successfully", classes));
    }

    @DeleteMapping("/classes/{courseId}/unenroll")
    public ResponseEntity<APIResponse<String>> unenrollFromClass(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer courseId) {
        studentClassService.unenrollFromClass(userDetails.getUsername(), courseId);
        return ResponseEntity.ok(new APIResponse<>(200, "Successfully unenrolled from class", null));
    }

    @PostMapping("/classes/join")
    public ResponseEntity<APIResponse<String>> joinClass(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody JoinClassDTO dto) {
        String responseMessage = studentClassService.joinClass(user.getUsername(), dto);
        return ResponseEntity.ok(new APIResponse<>(200, "Join request submitted", responseMessage));
    }

    @GetMapping("/classes/{classId}/people")
    public ResponseEntity<APIResponse<ClassPeopleDTO>> getClassPeople(@PathVariable Integer classId) {
        ClassPeopleDTO people = studentClassService.getClassPeople(classId);
        return ResponseEntity.ok(new APIResponse<>(200, "Roster loaded", people));
    }
}
