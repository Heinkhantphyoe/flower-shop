package com.hkp.flowershop.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponDto {
    private Long id;
    private String code;
    private double amount;
    private boolean active;
    private LocalDateTime expiresAt;
    private int usedCount;
}
