package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.FoodCategory;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.repository.FoodCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodCategoryService {

    @Autowired
    private FoodCategoryRepository foodCategoryRepository;

    /**
     * Get all food categories
     */
    @Transactional(readOnly = true)
    public List<FoodCategory> getAllCategories() {
        return foodCategoryRepository.findAll();
    }

    /**
     * Get food category by ID
     */
    @Transactional(readOnly = true)
    public FoodCategory getCategoryById(Long id) {
        return foodCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodCategory", "id", id));
    }

    /**
     * Get food category by name
     */
    @Transactional(readOnly = true)
    public FoodCategory getCategoryByName(String name) {
        return foodCategoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("FoodCategory", "name", name));
    }

    /**
     * Check if a category with the given name exists
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return foodCategoryRepository.existsByName(name);
    }

    /**
     * Create a new food category
     */
    @Transactional
    public FoodCategory createCategory(FoodCategory foodCategory) {
        return foodCategoryRepository.save(foodCategory);
    }

    /**
     * Update an existing food category
     */
    @Transactional
    public FoodCategory updateCategory(Long id, FoodCategory categoryDetails) {
        FoodCategory foodCategory = getCategoryById(id);

        foodCategory.setName(categoryDetails.getName());
        foodCategory.setDescription(categoryDetails.getDescription());
        foodCategory.setImageUrl(categoryDetails.getImageUrl());

        return foodCategoryRepository.save(foodCategory);
    }

    /**
     * Delete a food category
     */
    @Transactional
    public void deleteCategory(Long id) {
        FoodCategory category = getCategoryById(id);
        foodCategoryRepository.delete(category);
    }
}
