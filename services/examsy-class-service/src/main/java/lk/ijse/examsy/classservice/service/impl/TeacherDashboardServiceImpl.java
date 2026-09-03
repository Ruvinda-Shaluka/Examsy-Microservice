package lk.ijse.examsy.classservice.service.impl;

import lk.ijse.examsy.classservice.dto.CourseCreateDTO;
import lk.ijse.examsy.classservice.dto.TeacherClassCardDTO;
import lk.ijse.examsy.classservice.entity.Course;
import lk.ijse.examsy.classservice.repository.CourseRepo;
import lk.ijse.examsy.classservice.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    private final CourseRepo courseRepository;

    @Transactional(readOnly = true)
    @Override
    public List<TeacherClassCardDTO> getMyClasses(String username) {
        List<Course> courses = courseRepository.findByTeacherUsernameAndIsArchivedFalse(username);

        return courses.stream().map(course ->
                TeacherClassCardDTO.builder()
                        .id(course.getId())
                        .title(course.getName())
                        .section(course.getSectionName())
                        .themeColorHex(course.getThemeColorHex())
                        .bannerImageUrl(course.getBannerImageUrl())
                        .classCode(course.getClassCode())
                        .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TeacherClassCardDTO createClass(String username, CourseCreateDTO dto) {
        String uniqueCode = UUID.randomUUID().toString().substring(0, 7).toUpperCase();

        String[] colors = {"#4F46E5", "#059669", "#DC2626", "#D97706", "#7C3AED", "#2563EB", "#0891B2"};
        String randomColor = colors[new Random().nextInt(colors.length)];

        Course newCourse = Course.builder()
                .teacherUsername(username)
                .teacherName(username) // Default teacher display name to username until profile sync
                .name(dto.getName())
                .sectionName(dto.getSectionName())
                .academicTerm(dto.getAcademicTerm())
                .classCode(uniqueCode)
                .themeColorHex(randomColor)
                .classCodeUpdatedAt(LocalDateTime.now())
                .isArchived(false)
                .build();

        Course savedCourse = courseRepository.save(newCourse);
        log.info("Class '{}' created successfully by teacher '{}' with code {}", savedCourse.getName(), username, uniqueCode);

        return TeacherClassCardDTO.builder()
                .id(savedCourse.getId())
                .title(savedCourse.getName())
                .section(savedCourse.getSectionName())
                .themeColorHex(savedCourse.getThemeColorHex())
                .bannerImageUrl(savedCourse.getBannerImageUrl())
                .classCode(savedCourse.getClassCode())
                .build();
    }

    @Transactional
    @Override
    public void deleteClass(String username, Integer courseId) {
        Course course = courseRepository.findByIdAndTeacherUsername(courseId, username)
                .orElseThrow(() -> new RuntimeException("Class not found or unauthorized access."));

        courseRepository.delete(course);
        log.info("Class ID {} deleted successfully by teacher '{}'", courseId, username);
    }

    @Transactional
    @Override
    public void rotateExpiredClassCodes(String username) {
        // Will be implemented in the subsequent commit
    }
}
