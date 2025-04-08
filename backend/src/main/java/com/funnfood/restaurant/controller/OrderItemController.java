package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.OrderItem;
import com.funnfood.restaurant.service.OrderItemService;
import com.funnfood.restaurant.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {
    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    // Get all items for an order
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderItem>> getAllItemsByOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        List<OrderItem> orderItems = orderItemService.getItemsByOrder(order);
        return ResponseEntity.ok(orderItems);
    }

    // Get a specific order item
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        OrderItem orderItem = orderItemService.getItemById(id);
        return ResponseEntity.ok(orderItem);
    }

    // Add item to an order
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderItem> addItemToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItem orderItem) {

        Order order = orderService.getOrderById(orderId);
        OrderItem newOrderItem = orderItemService.addItemToOrder(order, orderItem);
        return new ResponseEntity<>(newOrderItem, HttpStatus.CREATED);
    }

    // Update order item
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderItem> updateOrderItem(
            @PathVariable Long id,
            @Valid @RequestBody OrderItem orderItem) {

        OrderItem updatedOrderItem = orderItemService.updateOrderItem(id, orderItem);
        return ResponseEntity.ok(updatedOrderItem);
    }

    // Delete order item
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrderItem(@PathVariable Long id) {
        orderItemService.removeOrderItem(id);
        return ResponseEntity.ok().build();
    }

    // Update quantity of an order item
    @PatchMapping("/{id}/quantity")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderItem> updateOrderItemQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {

        if (quantity < 1) {
            return ResponseEntity.badRequest().build();
        }

        OrderItem updatedOrderItem = orderItemService.updateOrderItemQuantity(id, quantity);
        return ResponseEntity.ok(updatedOrderItem);
    }
}
