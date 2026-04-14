package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.requests.UpdateProfileRequest;
import com.hkp.flowershop.dto.response.ProfileResponse;
import com.hkp.flowershop.exceptions.FileStorageException;
import com.hkp.flowershop.service.UserService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(Principal principal) {
        if (principal == null) {
            return ResponseUtil.unauthorized("Not authenticated");
        }

        try {
            return ResponseUtil.success(ProfileResponse.from(userService.getUserByEmail(principal.getName())));
        } catch (UsernameNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error while getting current user profile", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCurrentUserProfile(
            Principal principal,
            @Valid @ModelAttribute UpdateProfileRequest request
    ) {
        if (principal == null) {
            return ResponseUtil.unauthorized("Not authenticated");
        }

        try {
            return ResponseUtil.success(
                    ProfileResponse.from(userService.updateProfileByEmail(principal.getName(), request)),
                    "Profile updated successfully"
            );
        } catch (UsernameNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (FileStorageException | IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error while updating current user profile", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }
}
