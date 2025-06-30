package com.hkp.flowershop.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductFilterRequest {
    private String name;
    private Integer categoryId;
    private Integer minPrice;
    private Integer maxPrice;

}
