package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.Order;
import com.funnfood.restaurant.model.OrderItem;
import com.funnfood.restaurant.model.MenuItem;
import com.funnfood.restaurant.payload.request.OrderRequest;
import com.funnfood.restaurant.payload.response.OrderItemResponse;
import com.funnfood.restaurant.payload.response.OrderResponse;
import com.funnfood.restaurant.repository.MenuItemRepository;
import com.funnfood.restaurant.service.OrderItemService;
import com.funnfood.restaurant.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable Long userId) {
        // Additional authorization check needed here to verify the user is accessing their own orders
        List<Order> orders = orderService.getOrdersByUser(userId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        // Additional authorization check needed here to verify the manager is accessing their own restaurant
        List<Order> orders = orderService.getOrdersByRestaurant(restaurantId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            // Additional authorization check needed here to verify the user or manager is authorized
            return ResponseEntity.ok(convertToResponse(order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/token/{tokenNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOrderByToken(@PathVariable String tokenNumber) {
        try {
            Order order = orderService.getOrderByTokenNumber(tokenNumber);
            // Additional authorization check needed here to verify the user or manager is authorized
            return ResponseEntity.ok(convertToResponse(order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(
            @PathVariable Long userId,
            @Valid @RequestBody OrderRequest orderRequest) {
        try {
            // Convert request to order items
            Set<OrderItem> orderItems = new HashSet<>();
            for (com.funnfood.restaurant.payload.request.OrderItemRequest itemRequest : orderRequest.getItems()) {
                MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemRequest.getMenuItemId()));
                OrderItem orderItem = new OrderItem();
                orderItem.setMenuItem(menuItem);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
                orderItems.add(orderItem);
            }

            Order createdOrder = orderService.createOrder(
                    userId,
                    orderRequest.getRestaurantId(),
                    orderItems,
                    orderRequest.getDeliveryAddress(),
                    orderRequest.getDeliveryCity(),
                    orderRequest.getDeliveryState(),
                    orderRequest.getDeliveryZipCode(),
                    orderRequest.getContactPhone(),
                    orderRequest.getSpecialInstructions()
            );

            return new ResponseEntity<>(convertToResponse(createdOrder), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(convertToResponse(order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update order status: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/payment")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentInfo(
            @PathVariable Long id,
            @RequestParam String paymentId,
            @RequestParam String paymentStatus) {
        try {
            Order order = orderService.updatePaymentInfo(id, paymentId, paymentStatus);
            return ResponseEntity.ok(convertToResponse(order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update payment info: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order cancelledOrder = orderService.cancelOrder(id);
            return ResponseEntity.ok(convertToResponse(cancelledOrder));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to cancel order: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> addItemToOrder(
            @PathVariable Long id,
            @Valid @RequestBody com.funnfood.restaurant.payload.request.OrderItemRequest itemRequest) {
        try {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemRequest.getMenuItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());

            Order updatedOrder = orderService.addItemToOrder(id, orderItem);
            return ResponseEntity.ok(convertToResponse(updatedOrder));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to add item to order: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> removeItemFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        try {
            Order updatedOrder = orderService.removeItemFromOrder(orderId, itemId);
            return ResponseEntity.ok(convertToResponse(updatedOrder));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to remove item from order: " + e.getMessage()));
        }
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTokenNumber(order.getTokenNumber());
        response.setStatus(order.getStatus());
        response.setOrderDate(order.getOrderDate());

        if (order.getRestaurant() != null) {
            response.setRestaurantName(order.getRestaurant().getName());
        }

        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryCity(order.getDeliveryCity());
        response.setDeliveryState(order.getDeliveryState());
        response.setDeliveryZipCode(order.getDeliveryZipCode());
        response.setContactPhone(order.getContactPhone());
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentStatus(order.getPaymentStatus());

        // Convert order items to response objects
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);

        return response;
    }

    private OrderItemResponse convertToItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setQuantity(orderItem.getQuantity());
        response.setSpecialInstructions(orderItem.getSpecialInstructions());

        MenuItem menuItem = orderItem.getMenuItem();
        if (menuItem != null) {
            response.setMenuItemId(menuItem.getId());
            response.setMenuItemName(menuItem.getName());
            response.setMenuItemPrice(menuItem.getPrice());
            response.setMenuItemImage(menuItem.getImageUrl());
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
