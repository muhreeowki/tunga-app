package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.FoodCategory;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.service.FoodCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/food-categories")
public class FoodCategoryController {
    @Autowired
    private FoodCategoryService foodCategoryService;

    /**
     * Get all food categories
     */
    @GetMapping
    public ResponseEntity<List<FoodCategory>> getAllCategories() {
        List<FoodCategory> categories = foodCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get food category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodCategory> getCategoryById(@PathVariable Long id) {
        FoodCategory category = foodCategoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Get food category by name
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<FoodCategory> getCategoryByName(@PathVariable String name) {
        FoodCategory category = foodCategoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }

    /**
     * Create a new food category
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<FoodCategory> createCategory(@Valid @RequestBody FoodCategory foodCategory) {
        // Check if category name already exists
        if (foodCategoryService.existsByName(foodCategory.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(null); // In a real application, you would return an error message
        }

        FoodCategory newCategory = foodCategoryService.createCategory(foodCategory);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    /**
     * Update an existing food category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody FoodCategory foodCategory) {

        // Check if updated name conflicts with existing category
        if (!foodCategoryService.getCategoryById(id).getName().equals(foodCategory.getName())
                && foodCategoryService.existsByName(foodCategory.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Category name is already taken!"));
        }

        FoodCategory updatedCategory = foodCategoryService.updateCategory(id, foodCategory);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Delete a food category
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        foodCategoryService.deleteCategory(id);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully!"));
    }

    /**
     * Check if a category name exists
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<Boolean> checkCategoryExists(@PathVariable String name) {
        boolean exists = foodCategoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}
