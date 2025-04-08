package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiningRoomRepository extends JpaRepository<DiningRoom, Long> {
    Optional<DiningRoom> findByName(String name);
    List<DiningRoom> findByStatus(String status);
    List<DiningRoom> findByRestaurant(Restaurant restaurant);
}
