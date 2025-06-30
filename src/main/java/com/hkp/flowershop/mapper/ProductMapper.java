package com.hkp.flowershop.mapper;

import com.hkp.flowershop.dto.ProductDto;
import com.hkp.flowershop.dto.requests.CreateProductRequest;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.model.Product;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    @Autowired
    private ModelMapper modelMapper;

    public ProductDto toDto(Product product) {
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }

    public Product fromCreateRequest(CreateProductRequest request, Category category) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        return product;
    }
}

