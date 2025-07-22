package com.hkp.flowershop.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyResetToken {
    @NotBlank(message = "Token is required")
    private String token;
}
