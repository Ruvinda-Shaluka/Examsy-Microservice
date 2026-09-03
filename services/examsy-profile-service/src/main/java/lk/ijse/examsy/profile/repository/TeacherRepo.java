package lk.ijse.examsy.profile.repository;

import lk.ijse.examsy.profile.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepo extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findByUsername(String username);
    Optional<Teacher> findByEmail(String email);
    Optional<Teacher> findByUserId(Integer userId);
    boolean existsByUsername(String username);
    boolean existsByUserId(Integer userId);
}
