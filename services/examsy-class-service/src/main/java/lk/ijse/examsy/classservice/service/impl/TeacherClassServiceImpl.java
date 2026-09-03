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
        return null; // Will be implemented in next commit
    }

    @Transactional
    @Override
    public void removeStudentFromClass(String teacherUsername, Integer classId, Integer studentId) {
        // Will be implemented in next commit
    }

    @Transactional
    @Override
    public void inviteStudent(String teacherUsername, Integer classId, InviteStudentDTO dto) {
        // Will be implemented in next commit
    }

    @Transactional(readOnly = true)
    @Override
    public List<JoinRequestDTO> getPendingJoinRequests(String teacherUsername, Integer classId) {
        return Collections.emptyList();
    }

    @Transactional
    @Override
    public void approveJoinRequest(String teacherUsername, Integer requestId) {
        // Will be implemented in next commit
    }

    @Transactional
    @Override
    public void rejectJoinRequest(String teacherUsername, Integer requestId) {
        // Will be implemented in next commit
    }
}
