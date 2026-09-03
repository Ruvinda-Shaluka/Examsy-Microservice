package lk.ijse.examsy.profile.service;

import lk.ijse.examsy.profile.dto.TeacherDTO;

public interface TeacherProfileService {
    TeacherDTO getMyProfile(String username);
    TeacherDTO updateMyProfile(String username, TeacherDTO updateData);
}
