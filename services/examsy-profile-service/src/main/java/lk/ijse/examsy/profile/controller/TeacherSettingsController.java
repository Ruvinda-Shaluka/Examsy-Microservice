package lk.ijse.examsy.profile.controller;

import jakarta.validation.Valid;
import lk.ijse.examsy.common.dto.APIResponse;
import lk.ijse.examsy.profile.dto.TeacherDTO;
import lk.ijse.examsy.profile.service.TeacherProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Validated
public class TeacherSettingsController {

    private final TeacherProfileService teacherProfileService;

    @GetMapping("/me")
    public ResponseEntity<APIResponse<TeacherDTO>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        TeacherDTO profile = teacherProfileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(new APIResponse<>(200, "Profile Fetched Successfully", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<APIResponse<TeacherDTO>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TeacherDTO updateData) {
        TeacherDTO updatedProfile = teacherProfileService.updateMyProfile(userDetails.getUsername(), updateData);
        return ResponseEntity.ok(new APIResponse<>(200, "Profile Updated Successfully", updatedProfile));
    }
}
