package lk.ijse.examsy.gradingservice.controller;

import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.gradingservice.dto.MockExamRequestDTO;
import lk.ijse.examsy.gradingservice.dto.MockExamResponseDTO;
import lk.ijse.examsy.gradingservice.service.GroqMockExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mock-exams")
@RequiredArgsConstructor
@Slf4j
public class MockExamController {

    private final GroqMockExamService groqMockExamService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<APIResponse<MockExamResponseDTO>> generateExam(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MockExamRequestDTO request) {

        String username = userDetails != null ? userDetails.getUsername() : "student";
        log.info("Generating mock exam for user '{}', subject: '{}', count: {}",
                username, request.getSubject(), request.getCount());

        MockExamResponseDTO exam = groqMockExamService.generateAndSaveExam(
                username,
                request.getSubject(),
                request.getTopic(),
                request.getDifficulty(),
                request.getCount()
        );

        return ResponseEntity.ok(new APIResponse<>(200, "AI Exam Generated successfully", exam));
    }
}
