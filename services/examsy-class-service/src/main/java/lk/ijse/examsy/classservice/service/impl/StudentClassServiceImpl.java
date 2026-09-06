package lk.ijse.examsy.classservice.service.impl;

import lk.ijse.examsy.classservice.dto.ClassPeopleDTO;
import lk.ijse.examsy.classservice.dto.JoinClassDTO;
import lk.ijse.examsy.classservice.dto.PersonDTO;
import lk.ijse.examsy.classservice.dto.StudentClassCardDTO;
import lk.ijse.examsy.classservice.entity.ClassEnrollment;
import lk.ijse.examsy.classservice.entity.ClassJoinRequest;
import lk.ijse.examsy.classservice.entity.Course;
import lk.ijse.examsy.classservice.repository.ClassEnrollmentRepo;
import lk.ijse.examsy.classservice.repository.ClassJoinRequestRepo;
import lk.ijse.examsy.classservice.repository.CourseRepo;
import lk.ijse.examsy.classservice.service.StudentClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentClassServiceImpl implements StudentClassService {

    private final ClassEnrollmentRepo enrollmentRepository;
    private final CourseRepo courseRepository;
    private final ClassJoinRequestRepo classJoinRequestRepo;

    @Transactional(readOnly = true)
    @Override
    public List<StudentClassCardDTO> getMyEnrolledClasses(String username) {
        List<ClassEnrollment> enrollments = enrollmentRepository.findByStudentUsername(username);

        return enrollments.stream().map(enrollment -> {
            Course course = enrollment.getCourse();
            return StudentClassCardDTO.builder()
                    .id(course.getId())
                    .title(course.getName())
                    .section(course.getSectionName())
                    .themeColorHex(course.getThemeColorHex())
                    .bannerImageUrl(course.getBannerImageUrl())
                    .teacher(course.getTeacherName() != null ? course.getTeacherName() : "Unknown Instructor")
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void unenrollFromClass(String username, Integer courseId) {
        ClassEnrollment enrollment = enrollmentRepository
                .findByCourseIdAndStudentUsername(courseId, username)
                .orElseThrow(() -> new RuntimeException("Enrollment not found or unauthorized access."));

        enrollmentRepository.delete(enrollment);
        log.info("Student '{}' unenrolled from course ID {}", username, courseId);
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

        List<ClassEnrollment> enrollments = enrollmentRepository.findByCourseId(classId);

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
    public String joinClass(String username, JoinClassDTO dto) {
        String link = dto.getInviteLink().trim();
        Integer courseId;
        String extractedCode;

        try {
            String[] parts = link.split("/join/");
            if (parts.length < 2) throw new IllegalArgumentException();
            String[] params = parts[1].split("/");
            if (params.length < 2) throw new IllegalArgumentException();
            courseId = Integer.parseInt(params[0]);
            extractedCode = params[1].trim();
        } catch (Exception e) {
            throw new RuntimeException("Invalid invite link format.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Class not found."));

        if (course.getClassCode() == null || !course.getClassCode().equals(extractedCode)) {
            throw new RuntimeException("Invalid or expired invite link.");
        }

        if (enrollmentRepository.existsByCourseIdAndStudentUsername(courseId, username)) {
            throw new RuntimeException("You are already enrolled in this class.");
        }

        if (classJoinRequestRepo.existsByCourseIdAndStudentUsername(courseId, username)) {
            throw new RuntimeException("You have already sent a request to join this class. Please wait for the instructor to approve it.");
        }

        ClassJoinRequest request = ClassJoinRequest.builder()
                .course(course)
                .studentUsername(username)
                .studentName(username)
                .status("PENDING")
                .build();
        classJoinRequestRepo.save(request);
        log.info("Student '{}' submitted join request for class ID {}", username, courseId);

        return "Request sent successfully! You will be notified once the instructor approves your request.";
    }
}
