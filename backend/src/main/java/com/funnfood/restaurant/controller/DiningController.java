package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.payload.request.TableReservationRequest;
import com.funnfood.restaurant.payload.response.DiningRoomResponse;
import com.funnfood.restaurant.payload.response.DiningTableResponse;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.payload.response.TableReservationResponse;
import com.funnfood.restaurant.service.DiningService;
import com.funnfood.restaurant.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dining")
public class DiningController {

    @Autowired
    private DiningService diningService;

    @Autowired
    private UserService userService;

    // Get dining rooms by restaurant
    @GetMapping("/rooms/restaurant/{restaurantId}")
    public ResponseEntity<List<DiningRoomResponse>> getDiningRoomsByRestaurant(@PathVariable Long restaurantId) {
        List<DiningRoomResponse> diningRooms = diningService.getDiningRoomsByRestaurantId(restaurantId);
        return ResponseEntity.ok(diningRooms);
    }

    // Get tables by dining room
    @GetMapping("/tables/room/{roomId}")
    public ResponseEntity<List<DiningTableResponse>> getTablesByDiningRoom(@PathVariable Long roomId) {
        List<DiningTableResponse> tables = diningService.getTablesByDiningRoomId(roomId);
        return ResponseEntity.ok(tables);
    }

    // Get available tables for a specific date/time and number of guests
    @GetMapping("/tables/available")
    public ResponseEntity<List<DiningTableResponse>> getAvailableTables(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam Integer guests) {
        List<DiningTableResponse> availableTables = diningService.getAvailableTables(roomId, dateTime, guests);
        return ResponseEntity.ok(availableTables);
    }

    // Create a table reservation
    @PostMapping("/reservations")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> createReservation(
            @Valid @RequestBody TableReservationRequest reservationRequest) {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        TableReservationResponse reservation = diningService.reserveTable(currentUser, reservationRequest);
        return new ResponseEntity<>(reservation, HttpStatus.CREATED);
    }

    // Get the current user's reservations
    @GetMapping("/reservations/my")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<TableReservationResponse>> getMyReservations() {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        List<TableReservationResponse> reservations = diningService.getUserReservations(currentUser);
        return ResponseEntity.ok(reservations);
    }

    // Get a specific reservation by ID
    @GetMapping("/reservations/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> getReservationById(@PathVariable Long id) {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        TableReservationResponse reservation = diningService.getReservationById(id, currentUser);
        return ResponseEntity.ok(reservation);
    }

    // Cancel a reservation
    @PatchMapping("/reservations/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> cancelReservation(@PathVariable Long id) {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        TableReservationResponse cancelledReservation = diningService.cancelReservation(id, currentUser);
        return ResponseEntity.ok(cancelledReservation);
    }
}
