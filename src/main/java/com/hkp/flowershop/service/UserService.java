package com.hkp.flowershop.service;


import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {

    @Autowired
    UserRepo userRepo;


    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public Optional<User> findyByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}
