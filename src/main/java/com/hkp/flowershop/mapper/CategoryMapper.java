package com.hkp.flowershop.mapper;

import com.hkp.flowershop.dto.CategoryDto;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    @Autowired
    private CategoryService categoryService;

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        // Use optimized COUNT query instead of loading all products
        Long productCount = categoryService.getProductCountByCategoryId(category.getId());
        dto.setProductCount(productCount != null ? productCount.intValue() : 0);

        return dto;
    }
}
