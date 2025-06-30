package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.ProductDto;
import com.hkp.flowershop.dto.requests.CreateProductRequest;
import com.hkp.flowershop.dto.requests.PaginationRequest;
import com.hkp.flowershop.dto.requests.ProductFilterRequest;
import com.hkp.flowershop.dto.requests.UpdateProductRequest;
import com.hkp.flowershop.dto.response.PaginationResponse;
import com.hkp.flowershop.exceptions.FileStorageException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.mapper.ProductMapper;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.service.CategoryService;
import com.hkp.flowershop.service.ProductService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<?> getAllProducts(PaginationRequest paginationRequest, ProductFilterRequest productFilterRequest) {
        try {
            Pageable pageable = paginationRequest.toPageable();

            Page<Product> pagiProducts = productService.getAllProducts(productFilterRequest, pageable);

            List<ProductDto> productDtos = pagiProducts.stream()
                    .map(productMapper::toDto)
                    .toList();

            PaginationResponse<ProductDto> response = new PaginationResponse<>(productDtos, pagiProducts);
            return ResponseUtil.success(response);
        } catch (Exception e) {
            log.error("Error while fetching products", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @ModelAttribute CreateProductRequest request) {
        try {
            Optional<Category> optionalCategory = categoryService.findById(request.getCategoryId());
            if (optionalCategory.isEmpty()) {
                return ResponseUtil.notFound("Category not found");
            }

            Product product = productMapper.fromCreateRequest(request, optionalCategory.get());
            Product createdProduct = productService.createProductWithImage(product, request.getImage());

            ProductDto dto = productMapper.toDto(createdProduct);
            return ResponseUtil.created(dto, "Created product successfully");
        } catch (Exception e) {
            log.error("Error while creating product", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> productDetail(@PathVariable int id) {
        try {
            Optional<Product> optionalProduct = productService.findById(id);
            if (optionalProduct.isEmpty()) {
                return ResponseUtil.notFound("Product not found");
            }
            ProductDto productDto = productMapper.toDto(optionalProduct.get());
            return ResponseUtil.success(productDto);
        } catch (Exception e) {
            log.error("Error while getting product detail", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            productService.deleteProduct(id);
            return ResponseUtil.success("Product deleted successfully with Id " + id);
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error while deleting product", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @ModelAttribute UpdateProductRequest request) {
        try {
            Product updatedProduct = productService.updateProduct(id, request, request.getImage());
            ProductDto productDto = productMapper.toDto(updatedProduct);
            return ResponseUtil.success(productDto);
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (FileStorageException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error while updating product", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }
}
