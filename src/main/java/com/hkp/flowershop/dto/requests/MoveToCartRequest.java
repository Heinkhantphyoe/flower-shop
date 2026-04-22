package com.hkp.flowershop.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveToCartRequest {
    private Long wishlistId;
    private int quantity;
}
