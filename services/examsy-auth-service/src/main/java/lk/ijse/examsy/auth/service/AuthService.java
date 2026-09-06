package lk.ijse.examsy.auth.service;

import lk.ijse.examsy.auth.dto.request.AuthDTO;
import lk.ijse.examsy.auth.dto.request.StudentRegisterDTO;
import lk.ijse.examsy.auth.dto.request.TeacherRegisterDTO;
import lk.ijse.examsy.auth.dto.response.AuthResponseDTO;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    AuthResponseDTO authenticate(AuthDTO authDTO);

    @Transactional
    String registerStudent(StudentRegisterDTO dto);

    @Transactional
    String registerTeacher(TeacherRegisterDTO dto);

    @Transactional
    void initiatePasswordReset(String email);

    boolean verifyResetCode(String email, String code);

    @Transactional
    void resetPassword(String email, String code, String newPassword);
}
