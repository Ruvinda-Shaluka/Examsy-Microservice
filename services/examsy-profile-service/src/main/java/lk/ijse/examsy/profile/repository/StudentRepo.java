package lk.ijse.examsy.profile.repository;

import lk.ijse.examsy.profile.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<Student, Integer> {
    Optional<Student> findByUsername(String username);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUserId(Integer userId);
    boolean existsByUsername(String username);
    boolean existsByUserId(Integer userId);
}
