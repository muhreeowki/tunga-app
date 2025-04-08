package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.payload.response.MenuCategoryResponse;
import com.funnfood.restaurant.payload.response.MenuItemResponse;
import com.funnfood.restaurant.repository.MenuCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuCategoryService {

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    /**
     * Get all menu categories
     */
    @Transactional(readOnly = true)
    public List<MenuCategory> getAllCategories() {
        return menuCategoryRepository.findAll();
    }

    /**
     * Get menu category by ID
     */
    @Transactional(readOnly = true)
    public MenuCategory getCategoryById(Long id) {
        return menuCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", id));
    }

    /**
     * Get menu category by name
     */
    @Transactional(readOnly = true)
    public MenuCategory getCategoryByName(String name) {
        return menuCategoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "name", name));
    }

    /**
     * Check if a category with the given name exists
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return menuCategoryRepository.existsByName(name);
    }

    /**
     * Create a new menu category
     */
    @Transactional
    public MenuCategory createCategory(MenuCategory menuCategory) {
        return menuCategoryRepository.save(menuCategory);
    }

    /**
     * Update an existing menu category
     */
    @Transactional
    public MenuCategory updateCategory(Long id, MenuCategory categoryDetails) {
        MenuCategory menuCategory = getCategoryById(id);

        menuCategory.setName(categoryDetails.getName());
        menuCategory.setDescription(categoryDetails.getDescription());
        menuCategory.setImageUrl(categoryDetails.getImageUrl()); // Also update imageUrl

        return menuCategoryRepository.save(menuCategory);
    }

    /**
     * Delete a menu category
     */
    @Transactional
    public void deleteCategory(Long id) {
        MenuCategory category = getCategoryById(id);
        menuCategoryRepository.delete(category);
    }

    /**
     * Map MenuCategory to MenuCategoryResponse
     */
    public MenuCategoryResponse mapToCategoryResponse(MenuCategory category) {
        MenuCategoryResponse response = new MenuCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());

        List<MenuItemResponse> menuItemResponses = category.getMenuItems().stream()
                .filter(MenuItem::isAvailable)
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());

        response.setMenuItems(menuItemResponses);

        return response;
    }

    /**
     * Map MenuItem to MenuItemResponse
     */
    private MenuItemResponse mapToMenuItemResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setPrice(menuItem.getPrice());
        response.setVegetarian(menuItem.isVegetarian());
        response.setImageUrl(menuItem.getImageUrl());
        response.setServesPeople(menuItem.getServesPeople());
        response.setPreparationTimeMinutes(menuItem.getPreparationTimeMinutes());
        response.setAvailable(menuItem.isAvailable());

        if (menuItem.getCategory() != null) {
            response.setCategoryId(menuItem.getCategory().getId());
            response.setCategoryName(menuItem.getCategory().getName());
        }

        return response;
    }

    /**
     * Get all menu categories as response DTOs
     */
    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getAllCategoryResponses() {
        return getAllCategories().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a menu category by ID as response DTO
     */
    @Transactional(readOnly = true)
    public MenuCategoryResponse getCategoryResponseById(Long id) {
        return mapToCategoryResponse(getCategoryById(id));
    }
}
