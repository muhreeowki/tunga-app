package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.BadRequestException;
import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.*;
import com.funnfood.restaurant.payload.request.TableReservationRequest;
import com.funnfood.restaurant.payload.response.DiningRoomResponse;
import com.funnfood.restaurant.payload.response.DiningTableResponse;
import com.funnfood.restaurant.payload.response.TableReservationResponse;
import com.funnfood.restaurant.repository.DiningRoomRepository;
import com.funnfood.restaurant.repository.DiningTableRepository;
import com.funnfood.restaurant.repository.RestaurantRepository;
import com.funnfood.restaurant.repository.TableReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DiningService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DiningRoomRepository diningRoomRepository;

    @Autowired
    private DiningTableRepository diningTableRepository;

    @Autowired
    private TableReservationRepository tableReservationRepository;

    @Autowired
    private EmailService emailService;

    public List<DiningRoomResponse> getDiningRoomsByRestaurantId(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        return diningRoomRepository.findByRestaurant(restaurant).stream()
                .map(this::mapToDiningRoomResponse)
                .collect(Collectors.toList());
    }

    public List<DiningTableResponse> getTablesByDiningRoomId(Long diningRoomId) {
        DiningRoom diningRoom = diningRoomRepository.findById(diningRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", diningRoomId));

        return diningTableRepository.findByDiningRoom(diningRoom).stream()
                .map(this::mapToDiningTableResponse)
                .collect(Collectors.toList());
    }

    public List<DiningTableResponse> getAvailableTables(Long diningRoomId, LocalDateTime dateTime, int numberOfGuests) {
        DiningRoom diningRoom = diningRoomRepository.findById(diningRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", diningRoomId));

        // Find tables that are available and can accommodate the number of guests
        // Consider a reservation block of 2 hours
        LocalDateTime startTime = dateTime.minusHours(1);
        LocalDateTime endTime = dateTime.plusHours(1);

        return diningTableRepository.findAvailableTables(diningRoom, numberOfGuests, startTime, endTime).stream()
                .map(this::mapToDiningTableResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TableReservationResponse reserveTable(User user, TableReservationRequest request) {
        // Validate reservation time (6 hours in advance for current date, up to 1 month for future dates)
        validateReservationTime(request.getReservationDateTime());

        // Check if table exists and can accommodate the guests
        DiningTable table = diningTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", request.getTableId()));

        // Ensure DiningRoom is properly loaded with null checks
        if (table.getDiningRoom() == null) {
            throw new BadRequestException("The table is not associated with a dining room");
        }

        // Create effectively final temporary variable for dining room
        final DiningRoom tempDiningRoom = table.getDiningRoom();
        final Long diningRoomId = tempDiningRoom.getId();

        DiningRoom diningRoom = diningRoomRepository.findById(diningRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("DiningRoom", "id", diningRoomId));
        table.setDiningRoom(diningRoom);

        // Ensure Restaurant is properly loaded with null checks
        if (diningRoom.getRestaurant() == null) {
            throw new BadRequestException("The dining room is not associated with a restaurant");
        }

        // Create effectively final temporary variable for restaurant
        final Restaurant tempRestaurant = diningRoom.getRestaurant();
        final Long restaurantId = tempRestaurant.getId();

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));
        diningRoom.setRestaurant(restaurant);

        // Create and save the reservation
        TableReservation reservation = new TableReservation();
        reservation.setDiningTable(table);
        reservation.setUser(user);
        reservation.setReservationDateTime(request.getReservationDateTime());
        reservation.setNumberOfGuests(request.getNumberOfGuests());
        reservation.setTokenNumber(generateReservationToken());
        reservation.setStatus("CONFIRMED");
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setCreatedAt(LocalDateTime.now());

        TableReservation savedReservation = tableReservationRepository.save(reservation);

        // Send confirmation email
        sendReservationConfirmationEmail(user, savedReservation, restaurant);

        return mapToTableReservationResponse(savedReservation);
    }

    public List<TableReservationResponse> getUserReservations(User user) {
        return tableReservationRepository.findByUser(user).stream()
                .map(this::mapToTableReservationResponse)
                .collect(Collectors.toList());
    }

    public TableReservationResponse getReservationById(Long id, User user) {
        TableReservation reservation = tableReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Check if the reservation belongs to the user
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to view this reservation");
        }

        return mapToTableReservationResponse(reservation);
    }

    @Transactional
    public TableReservationResponse cancelReservation(Long id, User user) {
        TableReservation reservation = tableReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Check if the reservation belongs to the user
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to cancel this reservation");
        }

        // Check if the reservation can be cancelled (e.g., not too close to the reserved time)
        if (reservation.getReservationDateTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Reservations can only be cancelled at least 2 hours before the reserved time");
        }

        reservation.setStatus("CANCELLED");
        TableReservation updatedReservation = tableReservationRepository.save(reservation);

        return mapToTableReservationResponse(updatedReservation);
    }

    private void validateReservationTime(LocalDateTime reservationTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixHoursFromNow = now.plusHours(6);
        LocalDateTime oneMonthFromNow = now.plusMonths(1);

        // Check if the reservation is at least 6 hours in advance
        if (reservationTime.isBefore(sixHoursFromNow)) {
            throw new BadRequestException("Reservations must be made at least 6 hours in advance");
        }

        // Check if the reservation is within the next month
        if (reservationTime.isAfter(oneMonthFromNow)) {
            throw new BadRequestException("Reservations can only be made up to 1 month in advance");
        }
    }

    private String generateReservationToken() {
        // Generate a unique 6-character alphanumeric token
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder token = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            token.append(characters.charAt(random.nextInt(characters.length())));
        }

        return "RES-" + token.toString();
    }

    private void sendReservationConfirmationEmail(User user, TableReservation reservation, Restaurant restaurant) {
        String name = user.getUsername();
        String restaurantName = restaurant.getName();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String date = reservation.getReservationDateTime().format(dateFormatter);
        String time = reservation.getReservationDateTime().format(timeFormatter);

        emailService.sendReservationConfirmation(
                user.getEmail(),
                name,
                restaurantName,
                date,
                time,
                reservation.getNumberOfGuests(),
                reservation.getTokenNumber()
        );
    }

    private DiningRoomResponse mapToDiningRoomResponse(DiningRoom diningRoom) {
        DiningRoomResponse response = new DiningRoomResponse();
        response.setId(diningRoom.getId());
        response.setName(diningRoom.getName());
        response.setDescription(diningRoom.getDescription());

        // Handle null restaurant
        if (diningRoom.getRestaurant() != null) {
            response.setRestaurantId(diningRoom.getRestaurant().getId());
            response.setRestaurantName(diningRoom.getRestaurant().getName());
        } else {
            response.setRestaurantId(null);
            response.setRestaurantName("Unknown Restaurant");
        }

        return response;
    }

    private DiningTableResponse mapToDiningTableResponse(DiningTable table) {
        DiningTableResponse response = new DiningTableResponse();
        response.setId(table.getId());
        response.setTableNumber(table.getTableNumber());
        response.setCapacity(table.getCapacity());

        // Handle null dining room
        if (table.getDiningRoom() != null) {
            response.setDiningRoomId(table.getDiningRoom().getId());
            response.setDiningRoomName(table.getDiningRoom().getName());
        } else {
            response.setDiningRoomId(null);
            response.setDiningRoomName("Unknown Dining Room");
        }

        return response;
    }

    private TableReservationResponse mapToTableReservationResponse(TableReservation reservation) {
        TableReservationResponse response = new TableReservationResponse();
        response.setId(reservation.getId());
        response.setTokenNumber(reservation.getTokenNumber());
        response.setReservationDateTime(reservation.getReservationDateTime());
        response.setNumberOfGuests(reservation.getNumberOfGuests());
        response.setStatus(reservation.getStatus());
        response.setSpecialRequests(reservation.getSpecialRequests());
        response.setCreatedAt(reservation.getCreatedAt());

        // Set user info
        if (reservation.getUser() != null) {
            response.setUserId(reservation.getUser().getId());
            response.setUsername(reservation.getUser().getUsername());
        }

        // Set table info
        if (reservation.getDiningTable() != null) {
            DiningTable table = reservation.getDiningTable();
            response.setTableId(table.getId());
            response.setTableNumber(table.getTableNumber());

            // Set dining room info
            if (table.getDiningRoom() != null) {
                DiningRoom room = table.getDiningRoom();
                response.setDiningRoomId(room.getId());
                response.setDiningRoomName(room.getName());

                // Set restaurant info
                if (room.getRestaurant() != null) {
                    Restaurant restaurant = room.getRestaurant();
                    response.setRestaurantId(restaurant.getId());
                    response.setRestaurantName(restaurant.getName());
                } else {
                    response.setRestaurantName("Unknown Restaurant");
                }
            } else {
                response.setDiningRoomName("Unknown Dining Room");
                response.setDiningRoomId(null);
            }
        } else {
            response.setTableId(null);
            response.setTableNumber(null);
            response.setDiningRoomId(null);
            response.setDiningRoomName("Unknown Dining Room");
            response.setRestaurantId(null);
            response.setRestaurantName("Unknown Restaurant");
        }

        return response;
    }

}
