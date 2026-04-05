package com.hkp.flowershop.config;

import com.hkp.flowershop.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Runs daily at 2 AM to clean up expired and revoked refresh tokens
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens");
        refreshTokenService.cleanupExpiredTokens();
        log.info("Completed scheduled cleanup of expired refresh tokens");
    }
}
