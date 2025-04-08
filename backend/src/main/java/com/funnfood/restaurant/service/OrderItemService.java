package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.OrderItem;
import com.funnfood.restaurant.repository.MenuItemRepository;
import com.funnfood.restaurant.repository.OrderItemRepository;
import com.funnfood.restaurant.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    /**
     * Get all order items
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    /**
     * Get a specific order item by ID
     */
    @Transactional(readOnly = true)
    public OrderItem getItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", id));
    }

    /**
     * Get all items for a specific order
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByOrder(Order order) {
        return orderItemRepository.findByOrderId(order.getId());
    }

    /**
     * Add an item to an existing order
     */
    @Transactional
    public OrderItem addItemToOrder(Order order, @Valid OrderItem orderItem) {
        // Set the order for this item
        orderItem.setOrder(order);

        // If MenuItem was not set in the incoming orderItem, find it from the repository
        if (orderItem.getMenuItem() == null && orderItem.getMenuItem().getId() != null) {
            MenuItem menuItem = menuItemRepository.findById(orderItem.getMenuItem().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", orderItem.getMenuItem().getId()));
            orderItem.setMenuItem(menuItem);
        }

        // Save and return the new order item
        return orderItemRepository.save(orderItem);
    }

    /**
     * Update an existing order item
     */
    @Transactional
    public OrderItem updateOrderItem(Long id, @Valid OrderItem orderItemDetails) {
        OrderItem orderItem = getItemById(id);



        // The order and menu item shouldn't be changed after creation

        return orderItemRepository.save(orderItem);
    }

    /**
     * Update only the quantity of an order item
     */
    @Transactional
    public OrderItem updateOrderItemQuantity(Long id, int quantity) {
        OrderItem orderItem = getItemById(id);
        orderItem.setQuantity(quantity);
        return orderItemRepository.save(orderItem);
    }

    /**
     * Remove/delete an order item
     */
    @Transactional
    public void removeOrderItem(Long id) {
        OrderItem orderItem = getItemById(id);
        orderItemRepository.delete(orderItem);
    }

    // The following methods are preserved from the original implementation but may need adjustment
    // to align with the methods used by the controller

    /**
     * Get order item by ID (legacy method, delegates to getItemById)
     */
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long id) {
        return getItemById(id);
    }

    /**
     * Get order items for a specific order by order ID (legacy method)
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return getItemsByOrder(order);
    }

    /**
     * Create a new order item (legacy method)
     */
    @Transactional
    public OrderItem createOrderItem(Long orderId, Long menuItemId, int quantity, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", menuItemId));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(quantity);


        return orderItemRepository.save(orderItem);
    }

    /**
     * Update an existing order item (legacy method)
     */
    @Transactional
    public OrderItem updateOrderItem(Long id, int quantity, String notes) {
        OrderItem orderItem = getItemById(id);

        orderItem.setQuantity(quantity);


        return orderItemRepository.save(orderItem);
    }

    /**
     * Delete an order item (legacy method, delegates to removeOrderItem)
     */
    @Transactional
    public void deleteOrderItem(Long id) {
        removeOrderItem(id);
    }

    /**
     * Calculate total price for all items in an order
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderTotal(Long orderId) {
        List<OrderItem> orderItems = getOrderItemsByOrder(orderId);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            // If the OrderItem model doesn't have subtotal field directly,
            // calculate it here from price and quantity
            MenuItem menuItem = item.getMenuItem();
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }

        return total;
    }
}
