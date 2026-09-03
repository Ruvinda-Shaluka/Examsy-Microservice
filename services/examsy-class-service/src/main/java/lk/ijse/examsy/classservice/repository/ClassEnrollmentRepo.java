package lk.ijse.examsy.classservice.repository;

import lk.ijse.examsy.classservice.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEnrollmentRepo extends JpaRepository<ClassEnrollment, Integer> {
    List<ClassEnrollment> findByStudentUsername(String studentUsername);
    List<ClassEnrollment> findByCourseId(Integer courseId);
    Optional<ClassEnrollment> findByCourseIdAndStudentUsername(Integer courseId, String studentUsername);
    Optional<ClassEnrollment> findByCourseIdAndStudentId(Integer courseId, Integer studentId);
    boolean existsByCourseIdAndStudentUsername(Integer courseId, String studentUsername);
    long countByCourseId(Integer courseId);
}
