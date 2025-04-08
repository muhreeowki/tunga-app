package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByName(String name);

    List<Restaurant> findByCity(String city);

    @Query("SELECT r FROM Restaurant r WHERE r.city = :city AND " +
            "6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(r.latitude))) <= r.deliveryRadiusKm")
    List<Restaurant> findNearbyRestaurants(
            @Param("city") String city,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude);
}
