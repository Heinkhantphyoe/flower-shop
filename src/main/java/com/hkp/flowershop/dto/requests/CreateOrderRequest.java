package com.hkp.flowershop.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Order address must not be blank")
    private String orderAddress;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDto> orderItems;

    @Data
    public static class OrderItemDto {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @Positive(message = "Quantity must be greater than zero")
        private int quantity;
    }
}

