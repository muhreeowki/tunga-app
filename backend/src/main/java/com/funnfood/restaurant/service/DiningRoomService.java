package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.model.Restaurant;
import com.funnfood.restaurant.repository.DiningRoomRepository;
import com.funnfood.restaurant.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiningRoomService {
    @Autowired
    private DiningRoomRepository diningRoomRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<DiningRoom> getAllDiningRooms() {
        return diningRoomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DiningRoom getDiningRoomById(Long id) {
        return diningRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", id));
    }

    @Transactional(readOnly = true)
    public List<DiningRoom> getAvailableDiningRooms() {
        return diningRoomRepository.findByStatus("AVAILABLE");
    }

    @Transactional
    public DiningRoom createDiningRoom(DiningRoom diningRoom) {
        // Ensure restaurant is set properly
        if (diningRoom.getRestaurant() != null && diningRoom.getRestaurant().getId() != null) {
            Restaurant restaurant = restaurantRepository.findById(diningRoom.getRestaurant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", diningRoom.getRestaurant().getId()));
            diningRoom.setRestaurant(restaurant);
        } else {
            throw new IllegalArgumentException("Restaurant ID must be provided");
        }

        return diningRoomRepository.save(diningRoom);
    }

    @Transactional
    public DiningRoom updateDiningRoom(Long id, DiningRoom diningRoomDetails) {
        DiningRoom diningRoom = getDiningRoomById(id);

        diningRoom.setName(diningRoomDetails.getName());
        diningRoom.setStatus(diningRoomDetails.getStatus());
        diningRoom.setDescription(diningRoomDetails.getDescription());
        diningRoom.setCapacity(diningRoomDetails.getCapacity());

        // Update restaurant if provided
        if (diningRoomDetails.getRestaurant() != null && diningRoomDetails.getRestaurant().getId() != null) {
            Restaurant restaurant = restaurantRepository.findById(diningRoomDetails.getRestaurant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", diningRoomDetails.getRestaurant().getId()));
            diningRoom.setRestaurant(restaurant);
        }

        return diningRoomRepository.save(diningRoom);
    }

    @Transactional
    public void deleteDiningRoom(Long id) {
        DiningRoom diningRoom = getDiningRoomById(id);
        diningRoomRepository.delete(diningRoom);
    }

    @Transactional
    public DiningRoom updateStatus(Long id, String status) {
        DiningRoom diningRoom = getDiningRoomById(id);
        diningRoom.setStatus(status);
        return diningRoomRepository.save(diningRoom);
    }
}
