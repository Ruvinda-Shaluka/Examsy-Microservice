package lk.ijse.examsy.examservice.repository;

import lk.ijse.examsy.examservice.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepo extends JpaRepository<QuestionOption, Integer> {
    List<QuestionOption> findByQuestionId(Integer questionId);
}
