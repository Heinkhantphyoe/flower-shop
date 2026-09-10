package com.hkp.flowershop.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.hkp.flowershop.dto.response.LoginResponse;
import com.hkp.flowershop.enums.AuthProvider;
import com.hkp.flowershop.enums.Role;
import com.hkp.flowershop.enums.UserStatus;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.model.UserPrinciple;
import com.hkp.flowershop.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class GoogleAuthService {

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private GoogleIdTokenVerifier verifier;

    public LoginResponse loginWithGoogle(String idTokenString) {
        GoogleIdToken.Payload payload = verifyToken(idTokenString);
        User user = findOrCreateUser(payload);
        return buildLoginResponse(user);
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = getVerifier().verify(idTokenString);
            if (idToken == null) {
                throw new BadCredentialsException("Invalid or expired Google token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (payload.getEmail() == null || payload.getSubject() == null) {
                throw new BadCredentialsException("Google token is missing required claims.");
            }
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new BadCredentialsException("Google account email is not verified.");
            }
            return payload;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Google token verification failed", e);
            throw new BadCredentialsException("Google token verification failed.");
        }
    }

    /**
     * Account linking strategy:
     * 1. Match by googleId (subject) -> already linked, just log in.
     * 2. Match by email -> link the Google account to the existing user.
     * 3. No match -> create a new Google-only account (no password).
     */
    private User findOrCreateUser(GoogleIdToken.Payload payload) {
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 1. Existing Google-linked account
        Optional<User> byGoogleId = userRepo.findByGoogleId(googleId);
        if (byGoogleId.isPresent()) {
            return byGoogleId.get();
        }

        // 2. Email-based account linking
        Optional<User> byEmail = userRepo.findByEmail(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();

            // Same email claimed by a different Google account -> conflict.
            if (existing.getGoogleId() != null && !existing.getGoogleId().equals(googleId)) {
                throw new BadCredentialsException("This email is already linked to another Google account.");
            }

            // Link the Google account. Google verified this email, so the user becomes VERIFIED.
            existing.setGoogleId(googleId);
            existing.setStatus(UserStatus.VERIFIED);
            if (name != null) existing.setName(name);
            if (picture != null) existing.setProfileImageUrl(picture);
            return userRepo.save(existing);
        }

        // 3. Brand-new Google-only account
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name != null ? name : email);
        newUser.setGoogleId(googleId);
        newUser.setProvider(AuthProvider.GOOGLE);
        newUser.setRole(Role.ROLE_USER);
        newUser.setStatus(UserStatus.VERIFIED);
        newUser.setProfileImageUrl(picture);
        return userRepo.save(newUser);
    }

    private LoginResponse buildLoginResponse(User user) {
        UserPrinciple principal = new UserPrinciple(user);
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    private GoogleIdTokenVerifier getVerifier() throws IOException {
        if (verifier == null) {
            if (googleClientId == null || googleClientId.isBlank()) {
                log.error("GOOGLE_CLIENT_ID is not configured at app.google.client-id");
                throw new BadCredentialsException("Google login is not configured on the server.");
            }
            synchronized (this) {
                if (verifier == null) {
                    try {
                        verifier = new GoogleIdTokenVerifier.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                JacksonFactory.getDefaultInstance())
                                .setAudience(Collections.singletonList(googleClientId))
                                .build();
                    } catch (GeneralSecurityException e) {
                        log.error("Failed to create Google token verifier", e);
                        throw new IllegalStateException("Failed to initialize Google login.");
                    }
                }
            }
        }
        return verifier;
    }
}