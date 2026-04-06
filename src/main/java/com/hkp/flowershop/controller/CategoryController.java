package com.hkp.flowershop.controller;

import com.hkp.flowershop.dto.CategoryDto;
import com.hkp.flowershop.dto.requests.CreateCategoryRequest;
import com.hkp.flowershop.dto.requests.PaginationRequest;
import com.hkp.flowershop.dto.requests.UpdateCategoryRequest;
import com.hkp.flowershop.dto.response.PaginationResponse;
import com.hkp.flowershop.exceptions.ResourceNotFoundException;
import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.service.CategoryService;
import com.hkp.flowershop.service.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<?> getAllCategories(PaginationRequest paginationRequest) {
        try {
            Pageable pageable = paginationRequest.toPageable();
            Page<Category> pagiCategories = categoryService.getAllCategories(pageable);

            List<CategoryDto> categoryDtos = pagiCategories.stream()
                    .map(category -> modelMapper.map(category, CategoryDto.class))
                    .toList();

            PaginationResponse<CategoryDto> response = new PaginationResponse<>(categoryDtos, pagiCategories);
            return ResponseUtil.success(response);
        } catch (Exception e) {
            log.error("Error while fetching categories", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable int id) {
        try {
            Optional<Category> optionalCategory = categoryService.findById(id);
            if (optionalCategory.isEmpty()) {
                return ResponseUtil.notFound("Category not found");
            }
            CategoryDto categoryDto = modelMapper.map(optionalCategory.get(), CategoryDto.class);
            return ResponseUtil.success(categoryDto);
        } catch (Exception e) {
            log.error("Error while getting category detail", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        try {
            if (categoryService.existsByName(request.getName())) {
                return ResponseUtil.badRequest("Category with name '" + request.getName() + "' already exists");
            }

            Category category = modelMapper.map(request, Category.class);
            Category createdCategory = categoryService.createCategory(category);

            CategoryDto categoryDto = modelMapper.map(createdCategory, CategoryDto.class);
            return ResponseUtil.created(categoryDto, "Category created successfully");
        } catch (Exception e) {
            log.error("Error while creating category", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable int id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        try {
            Optional<Category> existingCategory = categoryService.findById(id);
            if (existingCategory.isEmpty()) {
                return ResponseUtil.notFound("Category not found");
            }

            if (!existingCategory.get().getName().equals(request.getName()) 
                    && categoryService.existsByName(request.getName())) {
                return ResponseUtil.badRequest("Category with name '" + request.getName() + "' already exists");
            }

            Category categoryDetails = modelMapper.map(request, Category.class);
            Category updatedCategory = categoryService.updateCategory(id, categoryDetails);

            CategoryDto categoryDto = modelMapper.map(updatedCategory, CategoryDto.class);
            return ResponseUtil.success(categoryDto, "Category updated successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error while updating category", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable int id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseUtil.success("Category deleted successfully with Id " + id);
        } catch (ResourceNotFoundException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("Error while deleting category", e);
            return ResponseUtil.internalError("Internal Server Error");
        }
    }
}
