package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.ProductDto;
import com.hkp.flowershop.dto.requests.CreateProductRequest;
import com.hkp.flowershop.dto.requests.PaginationRequest;
import com.hkp.flowershop.dto.requests.ProductFilterRequest;
import com.hkp.flowershop.dto.requests.UpdateProductRequest;
import com.hkp.flowershop.dto.response.PaginationResponse;
import com.hkp.flowershop.exceptions.FileStorageException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.service.CategoryService;
import com.hkp.flowershop.service.ProductService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    ModelMapper modelMapper;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(PaginationRequest paginationRequest, ProductFilterRequest filterRequest) {
        try {
            Pageable pageable = paginationRequest.toPageable();

            Page<Product> pagiProducts = productService.getAllProducts(
                    filterRequest.getName(),
                    filterRequest.getCategoryId(),
                    filterRequest.getMinPrice(),
                    filterRequest.getMaxPrice(),
                    pageable);

            List<ProductDto> productDtos = pagiProducts.stream().map(product -> {
                ProductDto dto = modelMapper.map(product, ProductDto.class);
                dto.setCategoryId(product.getCategory().getId());
                dto.setCategoryName(product.getCategory().getName());
                return dto;
            }).toList();
            PaginationResponse<ProductDto> response = new PaginationResponse<>(productDtos, pagiProducts);
            return ResponseUtil.success(response);
        } catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createFlower(@Valid @ModelAttribute CreateProductRequest request) {
        try {
            Optional<Category> result = categoryService.findById(request.getCategoryId());
            if (result.isEmpty()) {
                return ResponseUtil.notFound("Category not found");
            }
            Product product = new Product();
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setStock(request.getStock());
            product.setCategory(result.get());

            Product createdProduct = productService.createProductWithImage(product, request.getImage());
            ProductDto productDto = modelMapper.map(createdProduct, ProductDto.class);
            productDto.setCategoryId(createdProduct.getCategory().getId());
            productDto.setCategoryName(createdProduct.getCategory().getName());
            return ResponseUtil.created(productDto, "Created product successfully");
        } catch (Exception e) {
            log.info("Error while creating product");
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable int id) {
        try {
            Optional<Product> optionalProduct = productService.findById(id);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                ProductDto productDto = modelMapper.map(product, ProductDto.class);
                productDto.setCategoryId(product.getCategory().getId());
                return ResponseUtil.success(productDto);
            }
            return ResponseUtil.notFound("Product not found");
        } catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            productService.deleteProduct(id);
            return ResponseUtil.success("Product deleted successfully with Id" + id);
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @ModelAttribute UpdateProductRequest request) throws IOException {
        try {
            Product updatedProduct = productService.updateProduct(id, request, request.getImage());
            ProductDto productDto = modelMapper.map(updatedProduct, ProductDto.class);
            return ResponseUtil.success(productDto);
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (FileStorageException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalError("Internal Server Error");
        }
    }


}
