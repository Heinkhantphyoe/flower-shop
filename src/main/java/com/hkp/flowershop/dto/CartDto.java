package com.hkp.flowershop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CartDto {

    private List<CartItemsDto> cartItems;

}
