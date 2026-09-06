package lk.ijse.examsy.examservice.repository;

import lk.ijse.examsy.examservice.entity.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionAnswerRepo extends JpaRepository<SubmissionAnswer, Integer> {
    List<SubmissionAnswer> findBySubmissionId(Integer submissionId);
    Optional<SubmissionAnswer> findBySubmissionIdAndQuestionId(Integer submissionId, Integer questionId);
}
