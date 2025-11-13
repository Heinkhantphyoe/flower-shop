package com.hkp.flowershop.service;

import com.hkp.flowershop.exceptions.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${product.image.uploadDir}")
    private String uploadDir;

    private Path uploadPath;


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

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    public String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null) {
            throw new IllegalArgumentException("Original file name cannot be null");
        }
        String extension = originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : "";
        return UUID.randomUUID() + extension;
    }

    public String saveImage(MultipartFile file) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Could not store image file.", e);
        }
    }

    public void deleteImageFile(String imageUrl) {
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


}
