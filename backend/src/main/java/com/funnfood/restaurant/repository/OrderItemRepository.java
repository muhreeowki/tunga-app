package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(Long id);
}
