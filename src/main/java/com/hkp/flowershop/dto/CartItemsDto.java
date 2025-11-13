package com.hkp.flowershop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartItemsDto {

    private Long id;
    private String productName;
    private int quantity;
    private double price;
    private String imageUrl;
    private Long productId;

}


