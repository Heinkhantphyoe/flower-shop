package com.hkp.flowershop.controller;


import com.hkp.flowershop.dto.requests.LoginRequest;
import com.hkp.flowershop.dto.requests.RegisterRequest;
import com.hkp.flowershop.dto.response.LoginResponse;
import com.hkp.flowershop.enums.Role;
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
            user.setRole(Role.ROLE_USER);

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

            if (!userService.existsByEmail(loginRequest.getEmail())) {
                return ResponseUtil.badRequest("No such email.Register with your email");
            }

            // Authenticate user and get JWT token
            LoginResponse response = authService.verify(loginRequest.getEmail(),loginRequest.getPassword());


            return ResponseUtil.success(response,"Login successful");

        } catch (Exception e) {
            return ResponseUtil.badRequest("Username or Password incorrect");
        }
    }


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
