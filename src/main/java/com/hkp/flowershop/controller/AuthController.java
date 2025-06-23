package com.hkp.flowershop.controller;


import com.hkp.flowershop.dto.requests.LoginRequest;
import com.hkp.flowershop.dto.requests.RegisterRequest;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.service.AuthService;
import com.hkp.flowershop.service.UserService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    ModelMapper modelMapper;

    @GetMapping
    public String greeting() {
        return "Hello World";
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;



    /**
     * User Registration
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {

            //Check if user already exist
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseUtil.badRequest("Email already in use");
            }

            User user = modelMapper.map(request, User.class);

            // Register user
            authService.registerUser(user);

            return ResponseUtil.created("User registered successfully");

        } catch (Exception e) {
            return ResponseUtil.badRequest(e.getMessage());
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

            User user = modelMapper.map(loginRequest, User.class);
            // Authenticate user and get JWT token
            String  token = authService.verify(user);

            return ResponseUtil.success(token,"Login successful");

        } catch (Exception e) {
            return ResponseUtil.badRequest(e.getMessage());
        }
    }
//
//    /**
//     * User Logout
//     * POST /api/auth/logout
//     */
//    @PostMapping("/logout")
//    public ResponseEntity<ApiResponse> logoutUser(HttpServletRequest request) {
//        try {
//            // Extract JWT token from request
//            String token = extractTokenFromRequest(request);
//
//            if (token != null) {
//                // Add token to blacklist (optional)
//                authService.logoutUser(token);
//            }
//
//            return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully!", null));
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Logout failed: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Refresh JWT Token
//     * POST /api/auth/refresh-token
//     */
//    @PostMapping("/refresh-token")
//    public ResponseEntity<ApiResponse> refreshToken(HttpServletRequest request) {
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (token == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "No token provided!", null));
//            }
//
//            // Generate new token
//            LoginResponse refreshResponse = authService.refreshToken(token);
//
//            return ResponseEntity.ok(new ApiResponse(true,
//                    "Token refreshed successfully!", refreshResponse));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse(false, "Token refresh failed: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Email Verification
//     * GET /api/auth/verify-email?token={verificationToken}
//     */
//    @GetMapping("/verify-email")
//    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam("token") String verificationToken) {
//        try {
//            boolean isVerified = authService.verifyEmail(verificationToken);
//
//            if (isVerified) {
//                return ResponseEntity.ok(new ApiResponse(true,
//                        "Email verified successfully! You can now login.", null));
//            } else {
//                return ResponseEntity.badRequest()
//                        .body(new ApiResponse(false, "Invalid or expired verification token!", null));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Email verification failed: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Resend Email Verification
//     * POST /api/auth/resend-verification
//     */
//    @PostMapping("/resend-verification")
//    public ResponseEntity<ApiResponse> resendVerificationEmail(@RequestParam("email") String email) {
//        try {
//            authService.resendVerificationEmail(email);
//
//            return ResponseEntity.ok(new ApiResponse(true,
//                    "Verification email sent successfully!", null));
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Failed to send verification email: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Forgot Password - Send Reset Email
//     * POST /api/auth/forgot-password
//     */
//    @PostMapping("/forgot-password")
//    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam("email") String email) {
//        try {
//            authService.sendPasswordResetEmail(email);
//
//            return ResponseEntity.ok(new ApiResponse(true,
//                    "Password reset email sent successfully!", null));
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Failed to send reset email: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Reset Password
//     * POST /api/auth/reset-password
//     */
//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse> resetPassword(
//            @RequestParam("token") String resetToken,
//            @RequestParam("newPassword") String newPassword) {
//        try {
//            // Validate new password
//            validationUtil.validatePassword(newPassword);
//
//            boolean isReset = authService.resetPassword(resetToken, newPassword);
//
//            if (isReset) {
//                return ResponseEntity.ok(new ApiResponse(true,
//                        "Password reset successfully! You can now login with your new password.", null));
//            } else {
//                return ResponseEntity.badRequest()
//                        .body(new ApiResponse(false, "Invalid or expired reset token!", null));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Password reset failed: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Change Password (for authenticated users)
//     * POST /api/auth/change-password
//     */
//    @PostMapping("/change-password")
//    public ResponseEntity<ApiResponse> changePassword(
//            @RequestParam("currentPassword") String currentPassword,
//            @RequestParam("newPassword") String newPassword,
//            HttpServletRequest request) {
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (token == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "Authentication required!", null));
//            }
//
//            // Validate new password
//            validationUtil.validatePassword(newPassword);
//
//            boolean isChanged = authService.changePassword(token, currentPassword, newPassword);
//
//            if (isChanged) {
//                return ResponseEntity.ok(new ApiResponse(true,
//                        "Password changed successfully!", null));
//            } else {
//                return ResponseEntity.badRequest()
//                        .body(new ApiResponse(false, "Current password is incorrect!", null));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Password change failed: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Validate Token
//     * GET /api/auth/validate-token
//     */
//    @GetMapping("/validate-token")
//    public ResponseEntity<ApiResponse> validateToken(HttpServletRequest request) {
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (token == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "No token provided!", null));
//            }
//
//            boolean isValid = authService.validateToken(token);
//
//            if (isValid) {
//                return ResponseEntity.ok(new ApiResponse(true, "Token is valid!", null));
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "Token is invalid or expired!", null));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse(false, "Token validation failed: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Get Current User Info
//     * GET /api/auth/me
//     */
//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse> getCurrentUser(HttpServletRequest request) {
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (token == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "Authentication required!", null));
//            }
//
//            Object userInfo = authService.getCurrentUserInfo(token);
//
//            return ResponseEntity.ok(new ApiResponse(true,
//                    "User information retrieved successfully!", userInfo));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse(false, "Failed to get user info: " + e.getMessage(), null));
//        }
//    }
//
//    /**
//     * Helper method to extract JWT token from request
//     */
//    private String extractTokenFromRequest(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
}
