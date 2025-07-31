package com.hkp.flowershop.config;

import com.hkp.flowershop.enums.Role;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {


    private final UserRepo userRepo;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Value("${adminEmail}")
    private String adminEmail;

    @Value("${adminPassword}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (!userRepo.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(encoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            userRepo.save(admin);
            log.info("Admin created");
        }
    }
}
