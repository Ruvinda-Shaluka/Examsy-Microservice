package lk.ijse.examsy.profile.controller;

import jakarta.validation.Valid;
import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.profile.dto.StudentDTO;
import lk.ijse.examsy.profile.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Validated
public class StudentSettingsController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/me")
    public ResponseEntity<APIResponse<StudentDTO>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        StudentDTO profile = studentProfileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Profile Fetched Successfully", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<APIResponse<StudentDTO>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudentDTO updateData) {
        StudentDTO updatedProfile = studentProfileService.updateMyProfile(userDetails.getUsername(), updateData);
        return ResponseEntity.ok(new APIResponse<>(200, "Profile Updated Successfully", updatedProfile));
    }
}
