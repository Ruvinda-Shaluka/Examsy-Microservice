package lk.ijse.examsy.classservice.service;

import lk.ijse.examsy.classservice.dto.CourseCreateDTO;
import lk.ijse.examsy.classservice.dto.TeacherClassCardDTO;

import java.util.List;

public interface TeacherDashboardService {
    List<TeacherClassCardDTO> getMyClasses(String username);
    TeacherClassCardDTO createClass(String username, CourseCreateDTO dto);
    void deleteClass(String username, Integer courseId);
    void rotateExpiredClassCodes(String username);
}
