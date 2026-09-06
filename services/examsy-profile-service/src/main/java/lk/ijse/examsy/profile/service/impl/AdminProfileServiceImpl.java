package lk.ijse.examsy.profile.service.impl;

import lk.ijse.examsy.profile.dto.AdminProfileDTO;
import lk.ijse.examsy.profile.dto.AdminProfileUpdateDTO;
import lk.ijse.examsy.profile.entity.Admin;
import lk.ijse.examsy.profile.repository.AdminRepo;
import lk.ijse.examsy.profile.service.AdminProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProfileServiceImpl implements AdminProfileService {

    private final AdminRepo adminRepository;
    private final ModelMapper modelMapper;

    @Override
    public AdminProfileDTO getMyProfile(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin profile not found for username: " + username));
        return modelMapper.map(admin, AdminProfileDTO.class);
    }

    @Override
    @Transactional
    public AdminProfileDTO updateProfile(String username, AdminProfileUpdateDTO dto) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin profile not found for username: " + username));

        if (dto.getFullName() != null) admin.setFullName(dto.getFullName());
        if (dto.getProfilePictureUrl() != null) admin.setProfilePictureUrl(dto.getProfilePictureUrl());

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Updated Admin profile for username: [{}]", username);
        return modelMapper.map(savedAdmin, AdminProfileDTO.class);
    }
}
