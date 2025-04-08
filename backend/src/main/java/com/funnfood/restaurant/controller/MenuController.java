package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.payload.response.MenuCategoryResponse;
import com.funnfood.restaurant.payload.response.MenuItemResponse;
import com.funnfood.restaurant.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategoryResponse>> getAllCategories() {
        List<MenuCategoryResponse> categories = menuService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<MenuCategoryResponse> getCategoryById(@PathVariable Long id) {
        MenuCategoryResponse category = menuService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        List<MenuItemResponse> items = menuService.getItemsByCategory(categoryId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/vegetarian")
    public ResponseEntity<List<MenuItemResponse>> getVegetarianItems() {
        List<MenuItemResponse> items = menuService.getVegetarianItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> getItemById(@PathVariable Long id) {
        MenuItemResponse item = menuService.getMenuItemById(id);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<MenuCategoryResponse> createCategory(@Valid @RequestBody MenuCategory category) {
        MenuCategoryResponse newCategory = menuService.createCategory(category);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @PostMapping("/items/{categoryId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @Valid @RequestBody MenuItem menuItem,
            @PathVariable Long categoryId) {
        MenuItemResponse newItem = menuService.createMenuItem(menuItem, categoryId);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }
}
