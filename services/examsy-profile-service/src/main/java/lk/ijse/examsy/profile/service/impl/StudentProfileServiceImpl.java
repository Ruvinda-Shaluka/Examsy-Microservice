package lk.ijse.examsy.profile.service.impl;

import lk.ijse.examsy.profile.dto.StudentDTO;
import lk.ijse.examsy.profile.entity.Student;
import lk.ijse.examsy.profile.repository.StudentRepo;
import lk.ijse.examsy.profile.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentRepo studentRepository;
    private final ModelMapper modelMapper;

    @Override
    public StudentDTO getMyProfile(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student profile not found for username: " + username));

        return modelMapper.map(student, StudentDTO.class);
    }

    @Override
    @Transactional
    public StudentDTO updateMyProfile(String username, StudentDTO updateData) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student profile not found for username: " + username));

        // Progressive Profiling: Update fields if provided in request
        if (updateData.getFullName() != null) student.setFullName(updateData.getFullName());
        if (updateData.getMajor() != null) student.setMajor(updateData.getMajor());
        if (updateData.getAcademicBio() != null) student.setAcademicBio(updateData.getAcademicBio());
        if (updateData.getProfilePictureUrl() != null) student.setProfilePictureUrl(updateData.getProfilePictureUrl());
        if (updateData.getGender() != null) student.setGender(updateData.getGender());
        if (updateData.getDateOfBirth() != null) student.setDateOfBirth(updateData.getDateOfBirth());
        if (updateData.getGrade() != null) student.setGrade(updateData.getGrade());
        if (updateData.getStudentIdentificationNumber() != null) {
            student.setStudentIdentificationNumber(updateData.getStudentIdentificationNumber());
        }

        // Notification Preferences
        if (updateData.getNotifyEmail() != null) student.setNotifyEmail(updateData.getNotifyEmail());
        if (updateData.getNotifyPush() != null) student.setNotifyPush(updateData.getNotifyPush());
        if (updateData.getNotifyIdentity() != null) student.setNotifyIdentity(updateData.getNotifyIdentity());

        Student savedStudent = studentRepository.save(student);
        log.info("Updated Student profile for username: [{}]", username);
        return modelMapper.map(savedStudent, StudentDTO.class);
    }
}
