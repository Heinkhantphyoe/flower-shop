package com.hkp.flowershop.dto.requests;

import com.hkp.flowershop.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private double price;

    @NotNull(message = "Stock is required")
    @Positive(message = "Stock must be greater than 0")
    private double stock;

    @NotNull(message = "Image is required")
    private MultipartFile image;

    @NotNull(message = "Category Id is required")
    private Integer categoryId;


}

