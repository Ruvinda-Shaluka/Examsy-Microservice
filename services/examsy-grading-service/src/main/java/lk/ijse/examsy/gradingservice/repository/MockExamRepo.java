package lk.ijse.examsy.gradingservice.repository;

import lk.ijse.examsy.gradingservice.entity.MockExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockExamRepo extends JpaRepository<MockExam, Integer> {

    List<MockExam> findByStudentUsernameOrderByGeneratedAtDesc(String studentUsername);
}
