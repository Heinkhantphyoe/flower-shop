package com.hkp.flowershop.service;

import com.hkp.flowershop.dto.requests.UpdateProductRequest;
import com.hkp.flowershop.exceptions.FileStorageException;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.model.Product;
import com.hkp.flowershop.repository.CategoryRepo;
import com.hkp.flowershop.repository.ProductRepo;
import com.hkp.flowershop.service.specification.ProductSpecification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {

    @Autowired
    ProductRepo productRepo;

    @Autowired
    CategoryRepo categoryRepo;

    private Path uploadPath;


    @Value("${product.image.uploadDir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        createUploadDirectory(uploadPath);
    }


    private void createUploadDirectory(Path uploadPath) {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    @Transactional
    public Product createProductWithImage(Product product, MultipartFile imageFile) throws IOException {
        validateImageFile(imageFile);

        try {
            String fileName = saveImage(imageFile);
            product.setImageUrl(fileName);
            return productRepo.save(product);
        } catch (Exception e) {
            log.error("Error while saving image", e);
            throw new FileStorageException(e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null) {
            throw new IllegalArgumentException("Original file name cannot be null");
        }
        String extension = originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : "";
        return UUID.randomUUID() + extension;
    }


    public Page<Product> getAllProducts(
            String name,
            Integer categoryId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterBy(name, categoryId, minPrice, maxPrice);
        return productRepo.findAll(spec,pageable);
    }


    public Optional<Product> findById(int id) {
        return productRepo.findById(id);
    }

    @Transactional
    public void deleteProduct(int id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        deleteImageFile(product.getImageUrl());
        productRepo.delete(product);
    }

    private void deleteImageFile(String imageUrl) {
        try {
            // Only get the filename from the full image URL (e.g., /images/abc.jpg)
            String fileName = Paths.get(imageUrl).getFileName().toString();
            Path filePath = uploadPath.resolve(fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete image file: ", e);
        }
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
            validateImageFile(imageFile);

            // Delete old image in local machine
            deleteImageFile(product.getImageUrl());

            // Save new image
            String fileName = saveImage(imageFile);
            product.setImageUrl(fileName);
        }

        return productRepo.save(product);
    }

    private String saveImage(MultipartFile file) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            ;
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Could not store image file.", e);
        }
    }


}
