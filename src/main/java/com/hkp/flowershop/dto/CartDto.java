package com.hkp.flowershop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartDto {

    private Long id;
    private Long userId;
    private List<CartItemsDto> cartItems;

}
