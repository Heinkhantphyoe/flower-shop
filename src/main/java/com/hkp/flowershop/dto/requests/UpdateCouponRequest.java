package com.hkp.flowershop.dto.requests;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateCouponRequest {

    @Positive(message = "Discount amount must be greater than 0")
    private Double amount;

    private Boolean active;

    private LocalDateTime expiresAt;
}
