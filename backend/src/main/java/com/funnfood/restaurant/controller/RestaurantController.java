package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.payload.response.RestaurantResponse;
import com.funnfood.restaurant.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        List<RestaurantResponse> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantsByCity(@PathVariable String city) {
        List<RestaurantResponse> restaurants = restaurantService.getRestaurantsByCity(city);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<RestaurantResponse>> getNearbyRestaurants(
            @RequestParam String city,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        List<RestaurantResponse> restaurants = restaurantService.getNearbyRestaurants(city, latitude, longitude);
        return ResponseEntity.ok(restaurants);
    }
}
