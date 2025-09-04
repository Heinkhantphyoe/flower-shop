package com.hkp.flowershop.dto;


import com.hkp.flowershop.model.Category;
import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private double price;
    private double stock;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
}
