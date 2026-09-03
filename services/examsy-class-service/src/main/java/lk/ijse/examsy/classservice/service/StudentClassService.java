package lk.ijse.examsy.classservice.service;

import lk.ijse.examsy.classservice.dto.ClassPeopleDTO;
import lk.ijse.examsy.classservice.dto.JoinClassDTO;
import lk.ijse.examsy.classservice.dto.StudentClassCardDTO;

import java.util.List;

public interface StudentClassService {
    List<StudentClassCardDTO> getMyEnrolledClasses(String username);
    void unenrollFromClass(String username, Integer courseId);
    String joinClass(String username, JoinClassDTO dto);
    ClassPeopleDTO getClassPeople(Integer classId);
}
