package com.hkp.flowershop.controller;


import com.hkp.flowershop.dto.requests.*;
import com.hkp.flowershop.dto.response.LoginResponse;
import com.hkp.flowershop.service.AuthService;
import com.hkp.flowershop.service.EmailService;
import com.hkp.flowershop.service.UserService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {



    @GetMapping
    public String greeting() {
        return "Hello World";
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;


    /**
     * User Registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            String message = authService.register(request);
            return ResponseUtil.success(null, message);
        } catch (BadCredentialsException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Serever Error");
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {

        try {
            String message = authService.verifyOtp(request);
            return ResponseUtil.success(message);
        } catch (BadCredentialsException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Server Error....");
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> ResendOtp(@RequestBody ResendOtpRequest request) {
        try {
            String message = authService.resendOtp(request);
            return ResponseUtil.success(message);
        } catch (BadCredentialsException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Server Error...." + e.getMessage());
        }
    }


//

    /**
     * User Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.verify(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            return ResponseUtil.success(response, "Login successful");

        } catch (BadCredentialsException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String result = authService.forgotPassword(request.getEmail());
            return ResponseUtil.success(result);
        } catch (UsernameNotFoundException e) {
            return ResponseUtil.badRequest(e.getMessage());
        }catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String result = authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseUtil.success(result);
        } catch (BadCredentialsException e) {
            return ResponseUtil.badRequest(e.getMessage());
        }catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }

    }

}

