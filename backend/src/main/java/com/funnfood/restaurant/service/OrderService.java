package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.OrderItem;
import com.funnfood.restaurant.model.Restaurant;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.repository.MenuItemRepository;
import com.funnfood.restaurant.repository.OrderItemRepository;
import com.funnfood.restaurant.repository.OrderRepository;
import com.funnfood.restaurant.repository.RestaurantRepository;
import com.funnfood.restaurant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    /**
     * Get all orders in the system
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get orders for a specific user
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return orderRepository.findByUser(user);
    }

    /**
     * Get orders for a specific restaurant
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        return orderRepository.findByRestaurant(restaurant);
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get orders for a specific user with a specific status
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserAndStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return orderRepository.findByUserAndStatus(user, status);
    }

    /**
     * Get orders for a specific restaurant with a specific status
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByRestaurantAndStatus(Long restaurantId, String status) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        return orderRepository.findByRestaurantAndStatus(restaurant, status);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    /**
     * Get order by token number
     */
    @Transactional(readOnly = true)
    public Order getOrderByTokenNumber(String tokenNumber) {
        List<Order> orders = orderRepository.findByTokenNumber(tokenNumber);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("Order", "tokenNumber", tokenNumber);
        }
        return orders.get(0);
    }

    /**
     * Create a new order
     */
    @Transactional
    public Order createOrder(Long userId, Long restaurantId, Set<OrderItem> orderItems,
                             String deliveryAddress, String deliveryCity,
                             String deliveryState, String deliveryZipCode,
                             String contactPhone, String specialInstructions) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        // Create the order
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setTokenNumber(generateTokenNumber());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryCity(deliveryCity);
        order.setDeliveryState(deliveryState);
        order.setDeliveryZipCode(deliveryZipCode);
        order.setContactPhone(contactPhone);
        order.setSpecialInstructions(specialInstructions);
        order.setEstimatedDeliveryTime(calculateEstimatedDeliveryTime());

        // Calculate financial values
        BigDecimal subtotal = calculateSubtotal(orderItems);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal deliveryFee = calculateDeliveryFee(deliveryAddress, restaurant);
        BigDecimal totalAmount = subtotal.add(tax).add(deliveryFee);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("PENDING");

        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);

        // Save order items and associate with order
        Set<OrderItem> savedOrderItems = new HashSet<>();
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            savedOrderItems.add(orderItemRepository.save(item));
        }

        savedOrder.setOrderItems(savedOrderItems);
        return savedOrder;
    }

    /**
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /**
     * Update payment status and ID
     */
    @Transactional
    public Order updatePaymentInfo(Long orderId, String paymentId, String paymentStatus) {
        Order order = getOrderById(orderId);
        order.setPaymentId(paymentId);
        order.setPaymentStatus(paymentStatus);
        return orderRepository.save(order);
    }

    /**
     * Cancel an order
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);

        // Only allow cancellation of pending or confirmed orders
        if (order.getStatus().equals("PENDING") || order.getStatus().equals("CONFIRMED")) {
            order.setStatus("CANCELLED");
            return orderRepository.save(order);
        } else {
            throw new IllegalStateException("Cannot cancel an order that is already " + order.getStatus());
        }
    }

    /**
     * Generate a unique token number for the order
     */
    private String generateTokenNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Calculate subtotal based on order items
     */
    private BigDecimal calculateSubtotal(Set<OrderItem> orderItems) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            MenuItem menuItem = item.getMenuItem();
            BigDecimal itemPrice = menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemPrice);
        }
        return subtotal;
    }

    /**
     * Calculate tax amount (typically a percentage of subtotal)
     */
    private BigDecimal calculateTax(BigDecimal subtotal) {
        // Assuming 8% tax rate
        return subtotal.multiply(new BigDecimal("0.08")).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate delivery fee based on distance and restaurant
     */
    private BigDecimal calculateDeliveryFee(String deliveryAddress, Restaurant restaurant) {
        // Simple implementation - could be enhanced with distance calculation
        return new BigDecimal("5.99");
    }

    /**
     * Calculate estimated delivery time in minutes
     */
    private int calculateEstimatedDeliveryTime() {
        // Simple implementation - returns between 30-60 minutes
        return 30 + new Random().nextInt(31);
    }

    /**
     * Add an item to an existing order
     */
    @Transactional
    public Order addItemToOrder(Long orderId, OrderItem orderItem) {
        Order order = getOrderById(orderId);

        // Can only add items to pending orders
        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Cannot modify an order that is not pending");
        }

        orderItem.setOrder(order);
        OrderItem savedItem = orderItemRepository.save(orderItem);

        // Update order totals
        Set<OrderItem> updatedItems = order.getOrderItems();
        updatedItems.add(savedItem);
        order.setOrderItems(updatedItems);

        BigDecimal newSubtotal = calculateSubtotal(updatedItems);
        BigDecimal newTax = calculateTax(newSubtotal);
        BigDecimal deliveryFee = order.getDeliveryFee();
        BigDecimal newTotal = newSubtotal.add(newTax).add(deliveryFee);

        order.setSubtotal(newSubtotal);
        order.setTax(newTax);
        order.setTotalAmount(newTotal);

        return orderRepository.save(order);
    }

    /**
     * Remove an item from an existing order
     */
    @Transactional
    public Order removeItemFromOrder(Long orderId, Long orderItemId) {
        Order order = getOrderById(orderId);

        // Can only remove items from pending orders
        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Cannot modify an order that is not pending");
        }

        OrderItem itemToRemove = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));

        if (!itemToRemove.getOrder().getId().equals(orderId)) {
            throw new IllegalArgumentException("Item does not belong to the specified order");
        }

        // Remove the item
        Set<OrderItem> updatedItems = order.getOrderItems();
        updatedItems.remove(itemToRemove);
        order.setOrderItems(updatedItems);

        // Update totals
        BigDecimal newSubtotal = calculateSubtotal(updatedItems);
        BigDecimal newTax = calculateTax(newSubtotal);
        BigDecimal deliveryFee = order.getDeliveryFee();
        BigDecimal newTotal = newSubtotal.add(newTax).add(deliveryFee);

        order.setSubtotal(newSubtotal);
        order.setTax(newTax);
        order.setTotalAmount(newTotal);

        orderItemRepository.delete(itemToRemove);
        return orderRepository.save(order);
    }
}
