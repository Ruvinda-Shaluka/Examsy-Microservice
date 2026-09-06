package lk.ijse.examsy.examservice.repository;

import lk.ijse.examsy.examservice.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepo extends JpaRepository<Exam, Integer> {
    List<Exam> findByCourseId(Integer courseId);
    List<Exam> findByTeacherUsername(String teacherUsername);
    Optional<Exam> findByIdAndTeacherUsername(Integer id, String teacherUsername);
    List<Exam> findByCourseIdIn(List<Integer> courseIds);
}
