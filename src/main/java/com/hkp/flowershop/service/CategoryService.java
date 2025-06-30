package com.hkp.flowershop.service;

import com.hkp.flowershop.model.Category;
import com.hkp.flowershop.repository.CategoryRepo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    CategoryRepo categoryRepo;


    public Optional<Category> findById(int categoryId) {
        return categoryRepo.findById(categoryId);
    }
}
