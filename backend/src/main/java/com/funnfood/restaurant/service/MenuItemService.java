package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.repository.MenuCategoryRepository;
import com.funnfood.restaurant.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuItemService {
    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Transactional(readOnly = true)
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MenuItem getMenuItemEntity(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItemsByCategory(Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", categoryId));
        return menuItemRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getAvailableMenuItems() {
        return menuItemRepository.findByIsAvailable(true);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getVegetarianMenuItems() {
        return menuItemRepository.findByIsVegetarian(true);
    }

    @Transactional
    public MenuItem createMenuItem(MenuItem menuItem, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", categoryId));

        menuItem.setCategory(category);
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItem menuItemDetails) {
        MenuItem menuItem = getMenuItemEntity(id);

        menuItem.setName(menuItemDetails.getName());
        menuItem.setDescription(menuItemDetails.getDescription());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setVegetarian(menuItemDetails.isVegetarian());
        menuItem.setAvailable(menuItemDetails.isAvailable());
        menuItem.setImageUrl(menuItemDetails.getImageUrl());
        menuItem.setServesPeople(menuItemDetails.getServesPeople());
        menuItem.setPreparationTimeMinutes(menuItemDetails.getPreparationTimeMinutes());

        if (menuItemDetails.getCategory() != null) {
            MenuCategory category = menuCategoryRepository.findById(menuItemDetails.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", "id", menuItemDetails.getCategory().getId()));
            menuItem.setCategory(category);
        }

        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = getMenuItemEntity(id);
        menuItemRepository.delete(menuItem);
    }

    @Transactional
    public MenuItem updateAvailability(Long id, boolean isAvailable) {
        MenuItem menuItem = getMenuItemEntity(id);
        menuItem.setAvailable(isAvailable);
        return menuItemRepository.save(menuItem);
    }
}
