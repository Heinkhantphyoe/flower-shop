package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.requests.OtpVerificationRequest;
import com.hkp.flowershop.dto.requests.RegisterRequest;
import com.hkp.flowershop.dto.requests.ResendOtpRequest;
import com.hkp.flowershop.dto.response.LoginResponse;
import com.hkp.flowershop.enums.Role;
import com.hkp.flowershop.enums.UserStatus;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.model.UserPrinciple;
import com.hkp.flowershop.repository.UserRepo;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    @Autowired
    UserRepo repo;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    public JWTService jwtService;

    @Autowired
    RefreshTokenService refreshTokenService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    @Autowired
    private UserRepo userRepo;

    @Autowired
    EmailService emailService;

    @Value("${otp.expiry.minutes}")
    private long otpExpiryMinutes;

    @Value("${resetToken.expiry.minutes}")
    private long resetTokenExpiryMinutes;

    @Autowired
    private UserService userService;

    public String register(RegisterRequest request) {
        Optional<User> optionalUser = userRepo.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            if (existingUser.getStatus() == UserStatus.VERIFIED) {
                throw new BadCredentialsException("Email already in use.");
            }

            // Resend OTP to existing unverified user
            String otp = this.generateOtp();
            existingUser.setOtpCode(otp);
            existingUser.setOtpGeneratedAt(LocalDateTime.now());

            String htmlBody = "<html><body><p>Your OTP code for Flower Shop is: <strong>" + otp + "</strong></p></body></html>";
            emailService.sendCustomEmail(existingUser.getEmail(), "Flower Shop OTP", htmlBody);

            userRepo.save(existingUser);
            return "Email already registered. OTP resent. Please verify your email.";
        }

        // New user registration
        String otp = this.generateOtp();
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(encoder.encode(request.getPassword()));// encrypt password
        newUser.setName(request.getName());
        newUser.setRole(Role.ROLE_USER);
        newUser.setStatus(UserStatus.NOT_VERIFIED);
        newUser.setAddress(request.getAddress());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setOtpCode(otp);
        newUser.setOtpGeneratedAt(LocalDateTime.now());

        String htmlBody = "<html><body><p>Your OTP code for Flower Shop is: <strong>" + otp + "</strong></p></body></html>";
        emailService.sendCustomEmail(newUser.getEmail(), "Flower Shop OTP", htmlBody);

        userRepo.save(newUser);
        return "Registration successful. OTP has been sent to your email.";
    }


    public LoginResponse verify(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("No such email. Please register first."));

        if (user.getStatus() == UserStatus.NOT_VERIFIED) {
            throw new BadCredentialsException("Email not verified. Please complete OTP verification.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (!authentication.isAuthenticated()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate access token
        String accessToken = jwtService.generateToken(userPrinciple);
        
        // Generate and save refresh token
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(userPrinciple.getRole().name())
                .build();
    }

    public void saveUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
    }

    public String verifyOtp(OtpVerificationRequest request) {
        Optional<User> optionalUser = userRepo.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            throw new BadCredentialsException("Please check your email and register again.");
        }

        User user = optionalUser.get();

        boolean isOtpExpired = user.getOtpGeneratedAt()
                .plusMinutes(otpExpiryMinutes)
                .isBefore(LocalDateTime.now());

        if (!MessageDigest.isEqual(user.getOtpCode().getBytes(), request.getOtp().getBytes()) || isOtpExpired) {
            throw new BadCredentialsException("Invalid or expired OTP.");
        }

        user.setStatus(UserStatus.VERIFIED);
        user.setOtpCode(null);
        user.setOtpGeneratedAt(null);
        userRepo.save(user);

        return "Email verified successfully!";
    }


    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public String resendOtp(ResendOtpRequest request) {
        Optional<User> optionalUser = userRepo.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            throw new BadCredentialsException("Email not found. Please register first.");
        }

        User user = optionalUser.get();

        if (user.getStatus() == UserStatus.VERIFIED) {
            throw new BadCredentialsException("Email is already verified.");
        }

        String otp = this.generateOtp();

        userRepo.updateOtpInfo(otp,LocalDateTime.now(),user.getEmail());

        String htmlBody = "<html><body><p>Your OTP code for Flower Shop is: <strong>" + otp + "</strong></p></body></html>";
        emailService.sendCustomEmail(user.getEmail(), "Flower Shop OTP", htmlBody);


        return "OTP has been sent to your email.";
    }

    public String forgotPassword(String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("No user found with that email.");
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes));
        userRepo.save(user);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        String html = "<p>Click the link to reset your password:</p><a href=\"" + resetLink + "\">Reset Password</a>"+ token;
        emailService.sendCustomEmail(user.getEmail(), "Reset Your Password", html);

        return "Reset password link sent to your email.";
    }

    public String resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = userRepo.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired token.");
        }

        User user = optionalUser.get();
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Invalid or expire expired.");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);

        String loginLink = "http://localhost:5173/login";
        String html = "<p>Password Reset Succes.Login with new Password:</p><a href=\"" + loginLink + "\">Login</a>";
        emailService.sendCustomEmail(user.getEmail(), "Password Reset Successful", html);

        return "Password reset successful.";
    }

    public String verifyResetToken(String token) {
        log.info("token {}",token);
        Optional<User> optionalUser = userRepo.findByResetToken(token);

        if (optionalUser.isEmpty()) {
            log.error("no user found with this token par");
            throw new BadCredentialsException("Invalid or expired token.");
        }

        User user = optionalUser.get();
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Invalid or expire expired.");
        }
        return "Reset Token is valid";
    }
}
