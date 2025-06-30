package com.hkp.flowershop.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateProductRequest {

    private String name;

    private String description;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @Positive(message = "Stock must be greater than 0")
    private double stock;

    private MultipartFile image;

    @NotNull(message = "Category Id is required")
    private Integer categoryId;

}
