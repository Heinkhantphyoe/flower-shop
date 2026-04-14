package com.hkp.flowershop.service;


import com.hkp.flowershop.dto.requests.UpdateProfileRequest;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    FileStorageService fileStorageService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);


    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public Optional<User> findyByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
    }

    public User updateProfileByEmail(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());

        MultipartFile profileImage = request.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            fileStorageService.validateImageFile(profileImage);
            if (StringUtils.hasText(user.getProfileImageUrl())) {
                fileStorageService.deleteImageFile(user.getProfileImageUrl());
            }
            String imageFileName = fileStorageService.saveImage(profileImage);
            user.setProfileImageUrl(imageFileName);
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepo.save(user);
    }
}
