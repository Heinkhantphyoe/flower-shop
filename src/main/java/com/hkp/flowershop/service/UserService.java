package com.hkp.flowershop.service;

import com.hkp.flowershop.repository.UserRepo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;


    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }
}
