package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.MenuCategory;
import com.funnfood.restaurant.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryAndIsAvailable(MenuCategory category, boolean isAvailable);
    List<MenuItem> findByIsVegetarian(boolean isVegetarian);
    List<MenuItem> findByCategory(MenuCategory category);
    List<MenuItem> findByIsAvailable(boolean isAvailable);
}
