package lk.ijse.examsy.examservice.repository;

import lk.ijse.examsy.examservice.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSubmissionRepo extends JpaRepository<ExamSubmission, Integer> {
    Optional<ExamSubmission> findByExamIdAndStudentUsername(Integer examId, String studentUsername);
    List<ExamSubmission> findByExamId(Integer examId);
    List<ExamSubmission> findByStudentUsername(String studentUsername);
    List<ExamSubmission> findByExamIdAndStatus(Integer examId, String status);
}
