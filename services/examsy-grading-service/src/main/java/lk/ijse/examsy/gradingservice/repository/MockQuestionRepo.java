package lk.ijse.examsy.gradingservice.repository;

import lk.ijse.examsy.gradingservice.entity.MockQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockQuestionRepo extends JpaRepository<MockQuestion, Integer> {

    List<MockQuestion> findByMockExamId(Integer mockExamId);
}
