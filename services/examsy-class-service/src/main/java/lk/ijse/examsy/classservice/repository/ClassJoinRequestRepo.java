package lk.ijse.examsy.classservice.repository;

import lk.ijse.examsy.classservice.entity.ClassJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassJoinRequestRepo extends JpaRepository<ClassJoinRequest, Integer> {
    List<ClassJoinRequest> findByCourseIdAndStatusOrderByRequestedAtAsc(Integer courseId, String status);
    Optional<ClassJoinRequest> findByCourseIdAndStudentUsername(Integer courseId, String studentUsername);
    boolean existsByCourseIdAndStudentUsername(Integer courseId, String studentUsername);
}
