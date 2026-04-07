package com.hkp.flowershop.service;

import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.repository.CategoryRepo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    CategoryRepo categoryRepo;

    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepo.findAll(pageable);
    }

    public Optional<Category> findById(int categoryId) {
        return categoryRepo.findById(categoryId);
    }
    
    public Long getProductCountByCategoryId(Long categoryId) {
        return categoryRepo.countProductsByCategoryId(categoryId);
    }

    public Category createCategory(Category category) {
        return categoryRepo.save(category);
    }

    public Category updateCategory(int id, Category categoryDetails) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(categoryDetails.getName());
        return categoryRepo.save(category);
    }

    public void deleteCategory(int id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepo.delete(category);
    }

    public boolean existsByName(String name) {
        return categoryRepo.findByName(name).isPresent();
    }
}
