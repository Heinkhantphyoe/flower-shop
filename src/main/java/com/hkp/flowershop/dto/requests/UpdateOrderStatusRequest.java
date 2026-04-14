package com.hkp.flowershop.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "orderStatus is required")
    private Integer orderStatus;
}
