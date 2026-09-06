package lk.ijse.examsy.profile.service.impl;

import lk.ijse.examsy.profile.dto.TeacherDTO;
import lk.ijse.examsy.profile.entity.Teacher;
import lk.ijse.examsy.profile.repository.TeacherRepo;
import lk.ijse.examsy.profile.service.TeacherProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherProfileServiceImpl implements TeacherProfileService {

    private final TeacherRepo teacherRepository;
    private final ModelMapper modelMapper;

    @Override
    public TeacherDTO getMyProfile(String username) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for username: " + username));
        return modelMapper.map(teacher, TeacherDTO.class);
    }

    @Override
    @Transactional
    public TeacherDTO updateMyProfile(String username, TeacherDTO updateData) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for username: " + username));

        if (updateData.getFullName() != null) teacher.setFullName(updateData.getFullName());
        if (updateData.getSpecialization() != null) teacher.setSpecialization(updateData.getSpecialization());
        if (updateData.getOfficeLocation() != null) teacher.setOfficeLocation(updateData.getOfficeLocation());
        if (updateData.getProfessionalBio() != null) teacher.setProfessionalBio(updateData.getProfessionalBio());
        if (updateData.getProfilePictureUrl() != null) teacher.setProfilePictureUrl(updateData.getProfilePictureUrl());
        if (updateData.getInstructorId() != null) teacher.setInstructorId(updateData.getInstructorId());

        if (updateData.getNotifyEmail() != null) teacher.setNotifyEmail(updateData.getNotifyEmail());
        if (updateData.getNotifyPush() != null) teacher.setNotifyPush(updateData.getNotifyPush());
        if (updateData.getNotifySecurity() != null) teacher.setNotifySecurity(updateData.getNotifySecurity());

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Updated Teacher profile for username: [{}]", username);
        return modelMapper.map(savedTeacher, TeacherDTO.class);
    }
}
