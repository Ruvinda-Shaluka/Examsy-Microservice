package lk.ijse.examsy.classservice.service.impl;

import lk.ijse.examsy.classservice.dto.*;
import lk.ijse.examsy.classservice.entity.ClassAnnouncement;
import lk.ijse.examsy.classservice.entity.ClassEnrollment;
import lk.ijse.examsy.classservice.entity.ClassJoinRequest;
import lk.ijse.examsy.classservice.entity.Course;
import lk.ijse.examsy.classservice.repository.ClassAnnouncementRepo;
import lk.ijse.examsy.classservice.repository.ClassEnrollmentRepo;
import lk.ijse.examsy.classservice.repository.ClassJoinRequestRepo;
import lk.ijse.examsy.classservice.repository.CourseRepo;
import lk.ijse.examsy.classservice.service.TeacherClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherClassServiceImpl implements TeacherClassService {

    private final CourseRepo courseRepository;
    private final ClassAnnouncementRepo announcementRepository;
    private final ClassEnrollmentRepo classEnrollmentRepo;
    private final ClassJoinRequestRepo classJoinRequestRepo;
    private final JavaMailSender mailSender;

    @Transactional(readOnly = true)
    @Override
    public ClassStreamDTO getClassStream(Integer classId) {
        Course course = courseRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<AnnouncementDTO> announcementDTOs = announcementRepository.findByCourseIdOrderByCreatedAtDesc(classId)
                .stream().map(a -> AnnouncementDTO.builder()
                        .id(a.getId())
                        .authorName(a.getAuthorName())
                        .content(a.getContent())
                        .formattedDate(a.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")))
                        .build())
                .collect(Collectors.toList());

        return ClassStreamDTO.builder()
                .classCode(course.getClassCode())
                .title(course.getName())
                .section(course.getSectionName())
                .themeColorHex(course.getThemeColorHex())
                .bannerImageUrl(course.getBannerImageUrl())
                .announcements(announcementDTOs)
                .build();
    }

    @Transactional
    @Override
    public AnnouncementDTO postAnnouncement(Integer classId, String username, CreateAnnouncementDTO dto) {
        Course course = courseRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        ClassAnnouncement announcement = ClassAnnouncement.builder()
                .course(course)
                .authorUsername(username)
                .authorName(username)
                .content(dto.getContent())
                .build();

        announcement = announcementRepository.save(announcement);
        log.info("Announcement ID {} posted to class ID {} by teacher '{}'", announcement.getId(), classId, username);

        return AnnouncementDTO.builder()
                .id(announcement.getId())
                .authorName(announcement.getAuthorName())
                .content(announcement.getContent())
                .formattedDate("Just now")
                .build();
    }

    @Transactional
    @Override
    public AnnouncementDTO updateAnnouncement(Integer classId, Integer announcementId, String username, CreateAnnouncementDTO dto) {
        ClassAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));

        if (!announcement.getCourse().getId().equals(classId)) {
            throw new RuntimeException("Announcement does not belong to this class");
        }
        if (!announcement.getAuthorUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to edit this announcement");
        }

        announcement.setContent(dto.getContent());
        announcement = announcementRepository.save(announcement);

        return AnnouncementDTO.builder()
                .id(announcement.getId())
                .authorName(announcement.getAuthorName())
                .content(announcement.getContent())
                .formattedDate(announcement.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")) + " (Edited)")
                .build();
    }

    @Transactional
    @Override
    public void deleteAnnouncement(Integer classId, Integer announcementId, String username) {
        ClassAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));

        if (!announcement.getCourse().getId().equals(classId)) {
            throw new RuntimeException("Announcement does not belong to this class");
        }
        if (!announcement.getAuthorUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this announcement");
        }

        announcementRepository.delete(announcement);
    }

    @Transactional
    @Override
    public void updateClassAppearance(Integer classId, String username, UpdateAppearanceDTO dto) {
        Course course = courseRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (!course.getTeacherUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to modify this class");
        }

        course.setThemeColorHex(dto.getThemeColorHex());
        course.setBannerImageUrl(dto.getBannerImageUrl());
        courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    @Override
    public ClassPeopleDTO getClassPeople(Integer classId) {
        Course course = courseRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        PersonDTO teacherDto = PersonDTO.builder()
                .id(course.getTeacherId())
                .name(course.getTeacherName())
                .email(course.getTeacherUsername())
                .initial(course.getTeacherName() != null && !course.getTeacherName().isEmpty() ?
                        course.getTeacherName().substring(0, 1).toUpperCase() : "T")
                .role("Teacher")
                .profileImageUrl(null)
                .build();

        List<ClassEnrollment> enrollments = classEnrollmentRepo.findByCourseId(classId);

        List<PersonDTO> studentDtos = enrollments.stream().map(enrollment -> PersonDTO.builder()
                .id(enrollment.getStudentId())
                .name(enrollment.getStudentName())
                .email(enrollment.getStudentEmail() != null ? enrollment.getStudentEmail() : enrollment.getStudentUsername())
                .initial(enrollment.getStudentName() != null && !enrollment.getStudentName().isEmpty() ?
                        enrollment.getStudentName().substring(0, 1).toUpperCase() : "S")
                .role("Student")
                .profileImageUrl(enrollment.getStudentProfilePictureUrl())
                .build()
        ).collect(Collectors.toList());

        return ClassPeopleDTO.builder()
                .teachers(List.of(teacherDto))
                .students(studentDtos)
                .build();
    }

    @Transactional
    @Override
    public void removeStudentFromClass(String teacherUsername, Integer classId, Integer studentId) {
        courseRepository.findByIdAndTeacherUsername(classId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Class not found or unauthorized"));

        ClassEnrollment enrollment = classEnrollmentRepo.findByCourseIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new RuntimeException("Student is not enrolled in this class"));

        classEnrollmentRepo.delete(enrollment);
        log.info("Student ID {} removed from class ID {} by teacher '{}'", studentId, classId, teacherUsername);
    }

    @Transactional
    @Override
    public void inviteStudent(String teacherUsername, Integer classId, InviteStudentDTO dto) {
        Course course = courseRepository.findByIdAndTeacherUsername(classId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Class not found or unauthorized"));

        String activeClassCode = course.getClassCode();
        String inviteLink = "https://examsy.com/join/" + classId + "/" + activeClassCode;

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper =
                    new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new jakarta.mail.internet.InternetAddress("noreply@examsy.com", course.getTeacherName() + " (Examsy)"));
            helper.setTo(dto.getEmail());
            helper.setSubject("Invitation to join class: " + course.getName());

            String emailBody = "Hello,\n\n" +
                    "You have been invited by " + course.getTeacherName() +
                    " to join the class: " + course.getName() + ".\n\n" +
                    "Please copy the link below and paste it in your Examsy Student Dashboard to join:\n\n" +
                    inviteLink + "\n\n" +
                    "Welcome to the class!";

            helper.setText(emailBody);
            mailSender.send(message);
            log.info("Invitation email sent to {} for class '{}'", dto.getEmail(), course.getName());
        } catch (Exception e) {
            log.warn("Email dispatch error (skipped in local/mock): {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<JoinRequestDTO> getPendingJoinRequests(String teacherUsername, Integer classId) {
        courseRepository.findByIdAndTeacherUsername(classId, teacherUsername)
                .orElseThrow(() -> new RuntimeException("Unauthorized"));

        return classJoinRequestRepo.findByCourseIdAndStatusOrderByRequestedAtAsc(classId, "PENDING")
                .stream().map(req -> JoinRequestDTO.builder()
                        .requestId(req.getId())
                        .studentId(req.getStudentId())
                        .studentName(req.getStudentName())
                        .studentEmail(req.getStudentEmail())
                        .initial(req.getStudentName() != null && !req.getStudentName().isEmpty() ?
                                req.getStudentName().substring(0, 1).toUpperCase() : "S")
                        .requestedAt(req.getRequestedAt())
                        .build()
                ).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void approveJoinRequest(String teacherUsername, Integer requestId) {
        ClassJoinRequest request = classJoinRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCourse().getTeacherUsername().equals(teacherUsername)) {
            throw new RuntimeException("Unauthorized");
        }

        ClassEnrollment enrollment = ClassEnrollment.builder()
                .course(request.getCourse())
                .studentId(request.getStudentId())
                .studentUsername(request.getStudentUsername())
                .studentName(request.getStudentName())
                .studentEmail(request.getStudentEmail())
                .build();
        classEnrollmentRepo.save(enrollment);

        classJoinRequestRepo.delete(request);
        log.info("Join request ID {} approved by teacher '{}' for student '{}'", requestId, teacherUsername, request.getStudentUsername());
    }

    @Transactional
    @Override
    public void rejectJoinRequest(String teacherUsername, Integer requestId) {
        ClassJoinRequest request = classJoinRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCourse().getTeacherUsername().equals(teacherUsername)) {
            throw new RuntimeException("Unauthorized");
        }

        classJoinRequestRepo.delete(request);
        log.info("Join request ID {} rejected by teacher '{}' for student '{}'", requestId, teacherUsername, request.getStudentUsername());
    }
}
