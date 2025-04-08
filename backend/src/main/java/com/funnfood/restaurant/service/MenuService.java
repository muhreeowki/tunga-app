package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.payload.response.MenuCategoryResponse;
import com.funnfood.restaurant.payload.response.MenuItemResponse;
import com.funnfood.restaurant.repository.MenuCategoryRepository;
import com.funnfood.restaurant.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getAllCategories() {
        return menuCategoryRepository.findAll().stream()
                .map(this::mapToMenuCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuCategoryResponse getCategoryById(Long id) {
        MenuCategory category = menuCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", id));
        return mapToMenuCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getItemsByCategory(Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", categoryId));

        return menuItemRepository.findByCategoryAndIsAvailable(category, true).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getVegetarianItems() {
        return menuItemRepository.findByIsVegetarian(true).stream()
                .filter(MenuItem::isAvailable)
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        if (!menuItem.isAvailable()) {
            throw new ResourceNotFoundException("MenuItem", "id", id);
        }

        return mapToMenuItemResponse(menuItem);
    }

    @Transactional
    public MenuCategoryResponse createCategory(MenuCategory category) {
        MenuCategory savedCategory = menuCategoryRepository.save(category);
        return mapToMenuCategoryResponse(savedCategory);
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItem menuItem, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", categoryId));
        menuItem.setCategory(category);
        MenuItem savedItem = menuItemRepository.save(menuItem);
        return mapToMenuItemResponse(savedItem);
    }

    private MenuCategoryResponse mapToMenuCategoryResponse(MenuCategory category) {
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
}
