package lk.ijse.examsy.examservice.repository;

import lk.ijse.examsy.examservice.entity.ProctoringLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProctoringLogRepo extends JpaRepository<ProctoringLog, Integer> {
    List<ProctoringLog> findByExamSubmissionIdOrderByRecordedAtAsc(Integer submissionId);
}
