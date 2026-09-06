package lk.ijse.examsy.profile.service;

import lk.ijse.examsy.profile.dto.StudentDTO;

public interface StudentProfileService {
    StudentDTO getMyProfile(String username);
    StudentDTO updateMyProfile(String username, StudentDTO updateData);
}
