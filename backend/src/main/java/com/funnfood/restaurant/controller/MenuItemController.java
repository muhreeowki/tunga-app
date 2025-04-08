package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.payload.request.MenuItemRequest;
import com.funnfood.restaurant.payload.response.MenuItemResponse;
import com.funnfood.restaurant.service.MenuItemService;
import com.funnfood.restaurant.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/menu-items")  // Changed from "/api/menu/items" to "/api/menu-items"
public class MenuItemController {
    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        List<MenuItem> items = menuItemService.getAllMenuItems();
        List<MenuItemResponse> responses = items.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItem item = menuItemService.getMenuItemEntity(id);
            return ResponseEntity.ok(convertToResponse(item));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getMenuItemsByCategory(@PathVariable Long categoryId) {
        try {
            List<MenuItem> items = menuItemService.getMenuItemsByCategory(categoryId);
            List<MenuItemResponse> responses = items.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<MenuItemResponse>> getAvailableMenuItems() {
        List<MenuItem> items = menuItemService.getAvailableMenuItems();
        List<MenuItemResponse> responses = items.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/vegetarian")
    public ResponseEntity<List<MenuItemResponse>> getVegetarianMenuItems() {
        List<MenuItem> items = menuItemService.getVegetarianMenuItems();
        List<MenuItemResponse> responses = items.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItem menuItem = convertToEntity(request);
            MenuItem createdItem = menuItemService.createMenuItem(menuItem, request.getCategoryId());
            return new ResponseEntity<>(convertToResponse(createdItem), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create menu item: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItem menuItem = convertToEntity(request);
            MenuItem updatedItem = menuItemService.updateMenuItem(id, menuItem);
            return ResponseEntity.ok(convertToResponse(updatedItem));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update menu item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Menu item deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete menu item: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateMenuItemAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        try {
            MenuItem updatedItem = menuItemService.updateAvailability(id, available);
            return ResponseEntity.ok(convertToResponse(updatedItem));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update menu item availability: " + e.getMessage()));
        }
    }

    private MenuItem convertToEntity(MenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setVegetarian(request.isVegetarian());
        menuItem.setAvailable(request.isAvailable());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setServesPeople(request.getServesPeople());
        menuItem.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        return menuItem;
    }

    private MenuItemResponse convertToResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setPrice(menuItem.getPrice());
        response.setVegetarian(menuItem.isVegetarian());
        response.setImageUrl(menuItem.getImageUrl());
        response.setAvailable(menuItem.isAvailable());

        if (menuItem.getServesPeople() != null) {
            response.setServesPeople(menuItem.getServesPeople());
        }

        if (menuItem.getPreparationTimeMinutes() != null) {
            response.setPreparationTimeMinutes(menuItem.getPreparationTimeMinutes());
        }

        if (menuItem.getCategory() != null) {
            response.setCategoryId(menuItem.getCategory().getId());
            response.setCategoryName(menuItem.getCategory().getName());
        }

        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
