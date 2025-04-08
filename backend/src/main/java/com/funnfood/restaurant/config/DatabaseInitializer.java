package com.funnfood.restaurant.config;

import com.funnfood.restaurant.model.ERole;
import com.funnfood.restaurant.model.Role;
import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.repository.RoleRepository;
import com.funnfood.restaurant.repository.MenuCategoryRepository;
import com.funnfood.restaurant.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeMenuCategories();
        initializeMenuItems();
    }

    private void initializeRoles() {
        // Create default roles if they don't exist
        if (roleRepository.count() == 0) {
            Role userRole = new Role(ERole.ROLE_USER);
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            Role managerRole = new Role(ERole.ROLE_RESTAURANT_MANAGER);

            roleRepository.save(userRole);
            roleRepository.save(adminRole);
            roleRepository.save(managerRole);

            System.out.println("Roles initialized successfully");
        }
    }

    private void initializeMenuCategories() {
        if (menuCategoryRepository.count() == 0) {
            List<MenuCategory> categories = Arrays.asList(
                new MenuCategory("Appetizers", "Start your meal with our delicious appetizers"),
                new MenuCategory("Main Courses", "Our signature main dishes"),
                new MenuCategory("Desserts", "Sweet treats to end your meal"),
                new MenuCategory("Beverages", "Refreshing drinks and beverages")
            );

            menuCategoryRepository.saveAll(categories);
            System.out.println("Menu categories initialized successfully");
        }
    }

    private void initializeMenuItems() {
        if (menuItemRepository.count() == 0) {
            // Get categories
            MenuCategory appetizers = menuCategoryRepository.findByName("Appetizers")
                .orElseThrow(() -> new RuntimeException("Appetizers category not found"));
            MenuCategory mainCourses = menuCategoryRepository.findByName("Main Courses")
                .orElseThrow(() -> new RuntimeException("Main Courses category not found"));
            MenuCategory desserts = menuCategoryRepository.findByName("Desserts")
                .orElseThrow(() -> new RuntimeException("Desserts category not found"));
            MenuCategory beverages = menuCategoryRepository.findByName("Beverages")
                .orElseThrow(() -> new RuntimeException("Beverages category not found"));

            // Create menu items
            List<MenuItem> menuItems = Arrays.asList(
                // Appetizers
                createMenuItem("Bruschetta", "Toasted bread topped with tomatoes, garlic, and basil", 
                    new BigDecimal("8.99"), true, 2, 15, appetizers),
                createMenuItem("Calamari", "Crispy fried squid served with marinara sauce", 
                    new BigDecimal("12.99"), false, 2, 20, appetizers),
                createMenuItem("Spinach Artichoke Dip", "Creamy dip with spinach and artichokes", 
                    new BigDecimal("10.99"), true, 4, 15, appetizers),

                // Main Courses
                createMenuItem("Grilled Salmon", "Fresh salmon with lemon butter sauce", 
                    new BigDecimal("24.99"), false, 1, 25, mainCourses),
                createMenuItem("Vegetable Pasta", "Pasta with seasonal vegetables in garlic sauce", 
                    new BigDecimal("16.99"), true, 1, 20, mainCourses),
                createMenuItem("Beef Tenderloin", "Grass-fed beef with red wine reduction", 
                    new BigDecimal("32.99"), false, 1, 30, mainCourses),

                // Desserts
                createMenuItem("Tiramisu", "Classic Italian dessert with coffee and mascarpone", 
                    new BigDecimal("8.99"), true, 1, 10, desserts),
                createMenuItem("Chocolate Lava Cake", "Warm chocolate cake with vanilla ice cream", 
                    new BigDecimal("9.99"), true, 1, 15, desserts),
                createMenuItem("Cheesecake", "New York style cheesecake with berry compote", 
                    new BigDecimal("7.99"), true, 1, 10, desserts),

                // Beverages
                createMenuItem("Fresh Lemonade", "Homemade lemonade with mint", 
                    new BigDecimal("4.99"), true, 1, 5, beverages),
                createMenuItem("Iced Tea", "Freshly brewed iced tea", 
                    new BigDecimal("3.99"), true, 1, 5, beverages),
                createMenuItem("Sparkling Water", "Premium sparkling water", 
                    new BigDecimal("2.99"), true, 1, 5, beverages)
            );

            menuItemRepository.saveAll(menuItems);
            System.out.println("Menu items initialized successfully");
        }
    }

    private MenuItem createMenuItem(String name, String description, BigDecimal price, 
                                  boolean isVegetarian, Integer servesPeople, 
                                  Integer preparationTimeMinutes, MenuCategory category) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setVegetarian(isVegetarian);
        item.setServesPeople(servesPeople);
        item.setPreparationTimeMinutes(preparationTimeMinutes);
        item.setCategory(category);
        item.setAvailable(true);
        return item;
    }
}
