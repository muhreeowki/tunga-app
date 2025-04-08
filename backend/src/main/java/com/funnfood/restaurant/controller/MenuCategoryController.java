package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.payload.response.MenuCategoryResponse;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.service.MenuCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {
    @Autowired
    private MenuCategoryService menuCategoryService;

    /**
     * Get all menu categories
     */
    @GetMapping
    public ResponseEntity<List<MenuCategoryResponse>> getAllCategories() {
        List<MenuCategoryResponse> categories = menuCategoryService.getAllCategoryResponses();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get menu category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuCategoryResponse> getCategoryById(@PathVariable Long id) {
        MenuCategoryResponse category = menuCategoryService.getCategoryResponseById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Get menu category by name
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<MenuCategoryResponse> getCategoryByName(@PathVariable String name) {
        MenuCategory category = menuCategoryService.getCategoryByName(name);
        MenuCategoryResponse response = menuCategoryService.mapToCategoryResponse(category);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new menu category
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> createCategory(@Valid @RequestBody MenuCategory menuCategory) {
        // Check if category name already exists
        if (menuCategoryService.existsByName(menuCategory.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(null); // In a real application, you would return an error message
        }

        MenuCategory newCategory = menuCategoryService.createCategory(menuCategory);
        MenuCategoryResponse response = menuCategoryService.mapToCategoryResponse(newCategory);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing menu category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody MenuCategory menuCategory) {

        // Check if updated name conflicts with existing category
        if (!menuCategoryService.getCategoryById(id).getName().equals(menuCategory.getName())
                && menuCategoryService.existsByName(menuCategory.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Category name is already taken!"));
        }

        MenuCategory updatedCategory = menuCategoryService.updateCategory(id, menuCategory);
        MenuCategoryResponse response = menuCategoryService.mapToCategoryResponse(updatedCategory);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a menu category
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        menuCategoryService.deleteCategory(id);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully!"));
    }

    /**
     * Check if a category name exists
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<Boolean> checkCategoryExists(@PathVariable String name) {
        boolean exists = menuCategoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}
