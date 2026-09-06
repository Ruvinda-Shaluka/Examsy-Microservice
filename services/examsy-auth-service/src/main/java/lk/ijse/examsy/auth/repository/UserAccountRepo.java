package lk.ijse.examsy.auth.repository;

import lk.ijse.examsy.auth.entity.Role;
import lk.ijse.examsy.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepo extends JpaRepository<UserAccount, Integer> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<UserAccount> findByUsernameOrEmail(String username, String email);

    long countByRoleAndIsActiveTrue(Role role);

    long count();
}
