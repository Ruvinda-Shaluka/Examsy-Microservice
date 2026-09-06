package lk.ijse.examsy.examservice.repository;

import lk.ijse.examsy.examservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Integer> {
    List<Question> findByExamIdOrderByOrderIndexAsc(Integer examId);
}
