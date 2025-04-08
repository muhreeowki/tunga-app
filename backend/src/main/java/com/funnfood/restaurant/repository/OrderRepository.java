package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.Restaurant;
import com.funnfood.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findByRestaurant(Restaurant restaurant);

    List<Order> findByStatus(String status);

    List<Order> findByUserAndStatus(User user, String status);

    List<Order> findByRestaurantAndStatus(Restaurant restaurant, String status);

    List<Order> findByTokenNumber(String tokenNumber);
}
