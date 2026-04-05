package com.hkp.flowershop.service;

import com.hkp.flowershop.model.RefreshToken;
import com.hkp.flowershop.model.User;
import com.hkp.flowershop.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JWTService jwtService;

    @Value("${app.refreshToken.expiration}")
    private long refreshTokenExpirationMs;

    /**
     * Creates a new refresh token for the user
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke any existing active tokens for this user (optional - single device login)
        // refreshTokenRepository.deleteByUser(user);
        
        String tokenValue = jwtService.generateRefreshToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifies a refresh token and returns it if valid
     */
    public RefreshToken verifyRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found: {}", token);
            throw new BadCredentialsException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.getRevoked()) {
            log.warn("Attempted to use revoked refresh token: {}", token);
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Attempted to use expired refresh token: {}", token);
            throw new BadCredentialsException("Refresh token has expired");
        }

        return refreshToken;
    }

    /**
     * Rotates a refresh token - revokes the old one and creates a new one
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {
        RefreshToken oldRefreshToken = verifyRefreshToken(oldToken);
        
        // Revoke the old token
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        // Create and return a new token
        return createRefreshToken(oldRefreshToken.getUser());
    }

    /**
     * Revokes all refresh tokens for a user (logout all devices)
     */
    @Transactional
    public void revokeUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Cleanup expired and revoked tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Cleaned up expired and revoked refresh tokens");
    }
}
