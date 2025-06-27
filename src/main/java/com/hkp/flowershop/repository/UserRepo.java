package com.hkp.flowershop.repository;

import com.hkp.flowershop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    public Optional<User> findByName(String username);

    public Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByName(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.otpCode = :otp, u.otpGeneratedAt = :generatedAt WHERE u.email = :email")
    void updateOtpInfo(String otp,LocalDateTime generatedAt,String email );

    Optional<User> findByResetToken(String token);
}
