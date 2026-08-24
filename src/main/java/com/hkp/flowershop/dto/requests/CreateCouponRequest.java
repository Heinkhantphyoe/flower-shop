package com.hkp.flowershop.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @Positive(message = "Discount amount must be greater than 0")
    private double amount;

    private Boolean active = true;

    // Optional; coupons without expiry stay valid until deactivated
    private LocalDateTime expiresAt;
}
