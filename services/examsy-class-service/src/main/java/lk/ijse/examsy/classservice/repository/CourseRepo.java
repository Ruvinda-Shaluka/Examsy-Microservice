package lk.ijse.examsy.classservice.repository;

import lk.ijse.examsy.classservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<Course, Integer> {
    List<Course> findByTeacherUsernameAndIsArchivedFalse(String teacherUsername);
    List<Course> findByTeacherUsername(String teacherUsername);
    Optional<Course> findByIdAndTeacherUsername(Integer id, String teacherUsername);
    Optional<Course> findByClassCode(String classCode);
    boolean existsByClassCode(String classCode);
}
