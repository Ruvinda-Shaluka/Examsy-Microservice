package lk.ijse.examsy.profile.repository;

import lk.ijse.examsy.profile.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUserId(Integer userId);
    boolean existsByUsername(String username);
    boolean existsByUserId(Integer userId);
}
