package lk.ijse.examsy.auth.service.impl;

import lk.ijse.examsy.auth.dto.request.AuthDTO;
import lk.ijse.examsy.auth.dto.request.StudentRegisterDTO;
import lk.ijse.examsy.auth.dto.request.TeacherRegisterDTO;
import lk.ijse.examsy.auth.dto.response.AuthResponseDTO;
import lk.ijse.examsy.auth.entity.Role;
import lk.ijse.examsy.auth.entity.UserAccount;
import lk.ijse.examsy.auth.event.UserRegisteredEvent;
import lk.ijse.examsy.auth.kafka.AuthEventProducer;
import lk.ijse.examsy.auth.repository.UserAccountRepo;
import lk.ijse.examsy.auth.service.AuthService;
import lk.ijse.examsy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepo userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthEventProducer authEventProducer;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        String identifier = authDTO.getUsername();
        UserAccount user = userAccountRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        if (!passwordEncoder.matches(authDTO.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials for user: " + identifier);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return new AuthResponseDTO(token, user.getRole().name());
    }

    @Transactional
    @Override
    public String registerStudent(StudentRegisterDTO dto) {
        validateNewUser(dto.getUsername(), dto.getEmail());

        // 1. Save credentials in examsy_auth_db
        UserAccount account = UserAccount.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(Role.STUDENT)
                .authProvider("LOCAL")
                .isActive(true)
                .build();

        UserAccount savedAccount = userAccountRepository.save(account);

        // 2. Publish event to Kafka for Profile & Notification services
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedAccount.getId())
                .username(savedAccount.getUsername())
                .email(savedAccount.getEmail())
                .role(Role.STUDENT.name())
                .fullName(dto.getFullName())
                .studentIdentificationNumber(dto.getStudentIdentificationNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .grade(dto.getGrade())
                .registeredAt(LocalDateTime.now())
                .build();

        authEventProducer.publishUserRegistered(event);

        return "Student registered successfully";
    }

    @Transactional
    @Override
    public String registerTeacher(TeacherRegisterDTO dto) {
        validateNewUser(dto.getUsername(), dto.getEmail());

        // 1. Save credentials in examsy_auth_db
        UserAccount account = UserAccount.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(Role.TEACHER)
                .authProvider("LOCAL")
                .isActive(true)
                .build();

        UserAccount savedAccount = userAccountRepository.save(account);

        // 2. Publish event to Kafka for Profile & Notification services
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedAccount.getId())
                .username(savedAccount.getUsername())
                .email(savedAccount.getEmail())
                .role(Role.TEACHER.name())
                .fullName(dto.getFullName())
                .instructorId(dto.getInstructorId())
                .specialization(dto.getSpecialization())
                .registeredAt(LocalDateTime.now())
                .build();

        authEventProducer.publishUserRegistered(event);

        return "Teacher registered successfully";
    }

    private void validateNewUser(String username, String email) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    @Transactional
    @Override
    public void initiatePasswordReset(String email) {
        UserAccount user = userAccountRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new IllegalArgumentException("If this email exists, a reset code has been sent."));

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userAccountRepository.save(user);

        if (mailSender != null) {
            try {
                var message = mailSender.createMimeMessage();
                var helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(user.getEmail());
                helper.setSubject("Examsy Password Reset Code");
                helper.setText("Your password reset code is: " + code + "\n\nThis code expires in 15 minutes.");
                mailSender.send(message);
            } catch (Exception e) {
                log.error("Failed to send reset email to {}: {}", email, e.getMessage());
            }
        } else {
            log.warn("JavaMailSender not available; reset code for {} is: {}", email, code);
        }
    }

    @Override
    public boolean verifyResetCode(String email, String code) {
        UserAccount user = userAccountRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification request"));

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        if (LocalDateTime.now().isAfter(user.getResetCodeExpiresAt())) {
            throw new IllegalArgumentException("Verification code has expired");
        }
        return true;
    }

    @Transactional
    @Override
    public void resetPassword(String email, String code, String newPassword) {
        verifyResetCode(email, code);

        UserAccount user = userAccountRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);

        userAccountRepository.save(user);
    }
}
