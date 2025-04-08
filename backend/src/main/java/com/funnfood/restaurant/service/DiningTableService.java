package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.BadRequestException;
import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.model.DiningTable;
import com.funnfood.restaurant.model.Restaurant;
import com.funnfood.restaurant.payload.request.DiningTableRequest;
import com.funnfood.restaurant.repository.DiningRoomRepository;
import com.funnfood.restaurant.repository.DiningTableRepository;
import com.funnfood.restaurant.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiningTableService {

    @Autowired
    private DiningTableRepository diningTableRepository;

    @Autowired
    private DiningRoomRepository diningRoomRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<DiningTable> getAllTables() {
        return diningTableRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DiningTable getTableById(Long id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", id));
    }

    @Transactional(readOnly = true)
    public List<DiningTable> getTablesByDiningRoom(Long diningRoomId) {
        DiningRoom diningRoom = diningRoomRepository.findById(diningRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", diningRoomId));

        // Ensure restaurant is loaded
        if (diningRoom.getRestaurant() != null && diningRoom.getRestaurant().getId() != null) {
            Restaurant restaurant = restaurantRepository.findById(diningRoom.getRestaurant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", diningRoom.getRestaurant().getId()));
            diningRoom.setRestaurant(restaurant);
        }

        return diningTableRepository.findByDiningRoom(diningRoom);
    }

    @Transactional(readOnly = true)
    public List<DiningTable> getAvailableTables(Long diningRoomId, int capacity, LocalDateTime dateTime) {
        DiningRoom diningRoom = diningRoomRepository.findById(diningRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", diningRoomId));

        // Ensure restaurant is loaded
        if (diningRoom.getRestaurant() != null && diningRoom.getRestaurant().getId() != null) {
            Restaurant restaurant = restaurantRepository.findById(diningRoom.getRestaurant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", diningRoom.getRestaurant().getId()));
            diningRoom.setRestaurant(restaurant);
        }

        LocalDateTime startTime = dateTime.minusHours(1);
        LocalDateTime endTime = dateTime.plusHours(1);

        return diningTableRepository.findAvailableTables(diningRoom, capacity, startTime, endTime);
    }

    @Transactional
    public DiningTable createTable(DiningTableRequest tableRequest) {
        DiningRoom diningRoom = diningRoomRepository.findById(tableRequest.getDiningRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", tableRequest.getDiningRoomId()));

        // Ensure restaurant is loaded
        if (diningRoom.getRestaurant() != null && diningRoom.getRestaurant().getId() != null) {
            Restaurant restaurant = restaurantRepository.findById(diningRoom.getRestaurant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", diningRoom.getRestaurant().getId()));
            diningRoom.setRestaurant(restaurant);
        }

        // ✅ Uniqueness check goes here BEFORE saving
        if (diningTableRepository.existsByTableNumberAndDiningRoomId(
                tableRequest.getTableNumber(), tableRequest.getDiningRoomId())) {
            throw new BadRequestException("Table number already exists in this dining room.");
        }

        // Proceed with table creation
        DiningTable table = new DiningTable();
        table.setTableNumber(tableRequest.getTableNumber());
        table.setCapacity(tableRequest.getCapacity());
        table.setDiningRoom(diningRoom);

        return diningTableRepository.save(table);
    }


    @Transactional
    public DiningTable updateTable(Long id, DiningTableRequest tableRequest) {
        DiningTable table = getTableById(id);

        if (tableRequest.getTableNumber() != null) {
            table.setTableNumber(tableRequest.getTableNumber());
        }

        if (tableRequest.getCapacity() != null) {
            table.setCapacity(tableRequest.getCapacity());
        }

        if (tableRequest.getDiningRoomId() != null) {
            DiningRoom diningRoom = diningRoomRepository.findById(tableRequest.getDiningRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", tableRequest.getDiningRoomId()));

            // Ensure restaurant is loaded
            if (diningRoom.getRestaurant() != null && diningRoom.getRestaurant().getId() != null) {
                Restaurant restaurant = restaurantRepository.findById(diningRoom.getRestaurant().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", diningRoom.getRestaurant().getId()));
                diningRoom.setRestaurant(restaurant);
            }

            table.setDiningRoom(diningRoom);
        }


        return diningTableRepository.save(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        DiningTable table = getTableById(id);
        diningTableRepository.delete(table);
    }
}
