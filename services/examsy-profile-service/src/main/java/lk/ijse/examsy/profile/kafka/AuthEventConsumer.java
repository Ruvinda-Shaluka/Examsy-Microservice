package lk.ijse.examsy.profile.kafka;

import lk.ijse.examsy.profile.entity.Admin;
import lk.ijse.examsy.profile.entity.Student;
import lk.ijse.examsy.profile.entity.Teacher;
import lk.ijse.examsy.profile.event.UserRegisteredEvent;
import lk.ijse.examsy.profile.repository.AdminRepo;
import lk.ijse.examsy.profile.repository.StudentRepo;
import lk.ijse.examsy.profile.repository.TeacherRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEventConsumer {

    private final StudentRepo studentRepo;
    private final TeacherRepo teacherRepo;
    private final AdminRepo adminRepo;

    @KafkaListener(topics = "examsy.user.registered", groupId = "profile-service-group")
    public void consumeUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received user.registered event for user [{}], role: [{}]", event.getUsername(), event.getRole());

        try {
            if ("STUDENT".equalsIgnoreCase(event.getRole())) {
                provisionStudentProfile(event);
            } else if ("TEACHER".equalsIgnoreCase(event.getRole())) {
                provisionTeacherProfile(event);
            } else if ("ADMIN".equalsIgnoreCase(event.getRole())) {
                provisionAdminProfile(event);
            } else {
                log.warn("Unknown role [{}] in user.registered event for username [{}]", event.getRole(), event.getUsername());
            }
        } catch (Exception e) {
            log.error("Failed to provision profile for user [{}] with role [{}]: {}", event.getUsername(), event.getRole(), e.getMessage(), e);
        }
    }

    private void provisionStudentProfile(UserRegisteredEvent event) {
        if (studentRepo.existsByUserId(event.getUserId()) || studentRepo.existsByUsername(event.getUsername())) {
            log.info("Student profile already exists for username [{}] or userId [{}] - skipping duplicate event",
                    event.getUsername(), event.getUserId());
            return;
        }

        Student student = Student.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .fullName(event.getFullName() != null ? event.getFullName() : event.getUsername())
                .studentIdentificationNumber(event.getStudentIdentificationNumber())
                .dateOfBirth(event.getDateOfBirth())
                .gender(event.getGender())
                .grade(event.getGrade())
                .cumulativeGpa(BigDecimal.ZERO)
                .notifyEmail(true)
                .notifyPush(true)
                .notifyIdentity(true)
                .build();

        Student saved = studentRepo.save(student);
        log.info("Successfully provisioned Student profile with id [{}] for user [{}]", saved.getId(), event.getUsername());
    }

    private void provisionTeacherProfile(UserRegisteredEvent event) {
        if (teacherRepo.existsByUserId(event.getUserId()) || teacherRepo.existsByUsername(event.getUsername())) {
            log.info("Teacher profile already exists for username [{}] or userId [{}] - skipping duplicate event",
                    event.getUsername(), event.getUserId());
            return;
        }

        Teacher teacher = Teacher.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .fullName(event.getFullName() != null ? event.getFullName() : event.getUsername())
                .instructorId(event.getInstructorId())
                .specialization(event.getSpecialization())
                .notifyEmail(true)
                .notifyPush(true)
                .notifySecurity(true)
                .build();

        Teacher saved = teacherRepo.save(teacher);
        log.info("Successfully provisioned Teacher profile with id [{}] for user [{}]", saved.getId(), event.getUsername());
    }

    private void provisionAdminProfile(UserRegisteredEvent event) {
        if (adminRepo.existsByUserId(event.getUserId()) || adminRepo.existsByUsername(event.getUsername())) {
            log.info("Admin profile already exists for username [{}] or userId [{}] - skipping duplicate event",
                    event.getUsername(), event.getUserId());
            return;
        }

        Admin admin = Admin.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .fullName(event.getFullName() != null ? event.getFullName() : event.getUsername())
                .roleLevel("SUPER_ADMIN")
                .build();

        Admin saved = adminRepo.save(admin);
        log.info("Successfully provisioned Admin profile with id [{}] for user [{}]", saved.getId(), event.getUsername());
    }
}
