package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.Restaurant;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.payload.request.RestaurantRequest;
import com.funnfood.restaurant.payload.response.RestaurantResponse;
import com.funnfood.restaurant.repository.RestaurantRepository;
import com.funnfood.restaurant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return mapToRestaurantResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantByName(String name) {
        Restaurant restaurant = restaurantRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "name", name));
        return mapToRestaurantResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCity(city).stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getNearbyRestaurants(String city, double latitude, double longitude) {
        return restaurantRepository.findNearbyRestaurants(city, latitude, longitude).stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest restaurantRequest) {
        Restaurant restaurant = new Restaurant();
        mapRestaurantRequestToEntity(restaurantRequest, restaurant);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToRestaurantResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest restaurantRequest) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));

        mapRestaurantRequestToEntity(restaurantRequest, restaurant);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToRestaurantResponse(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        restaurantRepository.delete(restaurant);
    }

    private void mapRestaurantRequestToEntity(RestaurantRequest request, Restaurant restaurant) {
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setZipCode(request.getZipCode());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setLatitude(request.getLatitude() != null ? request.getLatitude() : 0);
        restaurant.setLongitude(request.getLongitude() != null ? request.getLongitude() : 0);
        restaurant.setDeliveryRadiusKm(request.getDeliveryRadiusKm() != null ? request.getDeliveryRadiusKm() : 5.0);
        restaurant.setAvgDeliveryTimeMin(request.getAvgDeliveryTimeMin() != null ? request.getAvgDeliveryTimeMin() : 30);

        // Set manager if provided
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));
            restaurant.setManager(manager);
        }
    }

    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setCity(restaurant.getCity());
        response.setState(restaurant.getState());
        response.setZipCode(restaurant.getZipCode());
        response.setPhoneNumber(restaurant.getPhoneNumber());
        response.setImageUrl(restaurant.getImageUrl());
        response.setLatitude(restaurant.getLatitude());
        response.setLongitude(restaurant.getLongitude());
        response.setDeliveryRadiusKm(restaurant.getDeliveryRadiusKm());
        response.setAvgDeliveryTimeMin(restaurant.getAvgDeliveryTimeMin());

        if (restaurant.getManager() != null) {
            response.setManagerId(restaurant.getManager().getId());
            response.setManagerName(restaurant.getManager().getUsername());
        }

        return response;
    }
}
