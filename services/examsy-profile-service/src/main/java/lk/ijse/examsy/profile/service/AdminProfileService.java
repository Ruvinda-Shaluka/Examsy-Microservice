package lk.ijse.examsy.profile.service;

import lk.ijse.examsy.profile.dto.AdminProfileDTO;
import lk.ijse.examsy.profile.dto.AdminProfileUpdateDTO;

public interface AdminProfileService {
    AdminProfileDTO getMyProfile(String username);
    AdminProfileDTO updateProfile(String username, AdminProfileUpdateDTO dto);
}
