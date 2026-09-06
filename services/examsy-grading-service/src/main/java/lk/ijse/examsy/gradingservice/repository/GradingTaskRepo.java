package lk.ijse.examsy.gradingservice.repository;

import lk.ijse.examsy.gradingservice.entity.GradingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradingTaskRepo extends JpaRepository<GradingTask, Integer> {

    Optional<GradingTask> findBySubmissionId(Integer submissionId);

    List<GradingTask> findByTeacherUsernameAndStatusOrderByCreatedAtDesc(String teacherUsername, String status);

    List<GradingTask> findByTeacherUsernameOrderByCreatedAtDesc(String teacherUsername);

    List<GradingTask> findByExamId(Integer examId);

    List<GradingTask> findByStatus(String status);
}
