package lk.ijse.examsy.auth.controller;

import jakarta.validation.Valid;
import lk.ijse.examsy.auth.dto.request.AuthDTO;
import lk.ijse.examsy.auth.dto.request.ForgotPasswordDTO;
import lk.ijse.examsy.auth.dto.request.ResetPasswordDTO;
import lk.ijse.examsy.auth.dto.request.StudentRegisterDTO;
import lk.ijse.examsy.auth.dto.request.TeacherRegisterDTO;
import lk.ijse.examsy.auth.dto.request.VerifyCodeDTO;
import lk.ijse.examsy.auth.dto.response.APIResponse;
import lk.ijse.examsy.auth.dto.response.AuthResponseDTO;
import lk.ijse.examsy.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/student")
    public ResponseEntity<APIResponse<String>> registerStudent(@Valid @RequestBody StudentRegisterDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Student registered successfully", authService.registerStudent(dto)));
    }

    @PostMapping("/signup/teacher")
    public ResponseEntity<APIResponse<String>> registerTeacher(@Valid @RequestBody TeacherRegisterDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Teacher registered successfully", authService.registerTeacher(dto)));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<APIResponse<AuthResponseDTO>> loginUser(@Valid @RequestBody AuthDTO authDTO) {
        return ResponseEntity.ok(new APIResponse<>(200, "Authentication successful", authService.authenticate(authDTO)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        authService.initiatePasswordReset(dto.getEmail());
        return ResponseEntity.ok(new APIResponse<>(200, "Password reset code sent", null));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<APIResponse<Void>> verifyCode(@Valid @RequestBody VerifyCodeDTO dto) {
        authService.verifyResetCode(dto.getEmail(), dto.getCode());
        return ResponseEntity.ok(new APIResponse<>(200, "Code verified successfully", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return ResponseEntity.ok(new APIResponse<>(200, "Password reset successfully", null));
    }
}
