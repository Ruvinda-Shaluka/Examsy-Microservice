package lk.ijse.examsy.examservice.controller;

import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.examservice.dto.CalendarExamDTO;
import lk.ijse.examsy.examservice.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student/dashboard/calendar")
@RequiredArgsConstructor
public class StudentCalendarController {

    private final StudentExamService studentExamService;

    @GetMapping("/exams")
    public ResponseEntity<APIResponse<List<CalendarExamDTO>>> getCalendarExams(
            @AuthenticationPrincipal UserDetails user) {
        List<CalendarExamDTO> exams = studentExamService.getStudentCalendarExams(user.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Calendar exams fetched successfully", exams));
    }
}
