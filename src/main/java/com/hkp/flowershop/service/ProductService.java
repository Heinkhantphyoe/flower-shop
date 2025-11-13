package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.requests.ProductFilterRequest;
import com.hkp.flowershop.dto.requests.UpdateProductRequest;
import com.hkp.flowershop.exceptions.FileStorageException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.repository.CategoryRepo;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.service.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {


    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final FileStorageService fileStorageService;

    @Transactional
    public Product createProductWithImage(Product product, MultipartFile imageFile) throws IOException {
        fileStorageService.validateImageFile(imageFile);

        try {
            String fileName = fileStorageService.saveImage(imageFile);
            product.setImageUrl(fileName);
            return productRepo.save(product);
        } catch (Exception e) {
            log.error("Error while saving image", e);
            throw new FileStorageException(e.getMessage());
        }
    }



    public Page<Product> getAllProducts(
            ProductFilterRequest request,
            Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterBy(request.getName(), request.getCategoryId(), request.getMinPrice(), request.getMaxPrice());
        return productRepo.findAll(spec,pageable);
    }


    public Optional<Product> findById(int id) {
        return productRepo.findById(id);
    }

    @Transactional
    public void deleteProduct(int id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        fileStorageService.deleteImageFile(product.getImageUrl());
        productRepo.delete(product);
    }




    @Transactional
    public Product updateProduct(int id, UpdateProductRequest request, MultipartFile imageFile) throws IOException {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update other fields
        product.setId((long) id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        product.setCategory(category);

        // If new image is provided, replace the old one
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorageService.validateImageFile(imageFile);

            // Delete old image in local machine
            fileStorageService.deleteImageFile(product.getImageUrl());

            // Save new image
            String fileName = fileStorageService.saveImage(imageFile);
            product.setImageUrl(fileName);
        }

        return productRepo.save(product);
    }


}
