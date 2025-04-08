package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.TableReservation;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.payload.request.TableReservationRequest;
import com.funnfood.restaurant.payload.response.TableReservationResponse;
import com.funnfood.restaurant.service.TableReservationService;
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
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reservations")
public class TableReservationController {
    @Autowired
    private TableReservationService tableReservationService;

    @Autowired
    private UserService userService;

    // Get all reservations (admin/manager only)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<TableReservationResponse>> getAllReservations() {
        List<TableReservation> reservations = tableReservationService.getAllReservations();
        List<TableReservationResponse> responseList = mapToResponseList(reservations);
        return ResponseEntity.ok(responseList);
    }

    // Get reservation by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> getReservationById(@PathVariable Long id) {
        TableReservation reservation = tableReservationService.getReservationById(id);

        // Check if the current user has permission to view this reservation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (reservation.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_MANAGER"))) {
            return ResponseEntity.ok(mapToResponse(reservation));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Get current user's reservations
    @GetMapping("/my-reservations")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<TableReservationResponse>> getMyReservations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        List<TableReservation> reservations = tableReservationService.getReservationsByUser(currentUser);
        List<TableReservationResponse> responseList = mapToResponseList(reservations);
        return ResponseEntity.ok(responseList);
    }

    // Create a reservation
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> createReservation(
            @Valid @RequestBody TableReservationRequest reservationRequest) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);

        TableReservation reservation = tableReservationService.createReservation(currentUser, reservationRequest);
        return new ResponseEntity<>(mapToResponse(reservation), HttpStatus.CREATED);
    }

    // Update reservation status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        TableReservation updatedReservation = tableReservationService.updateReservationStatus(id, status);
        return ResponseEntity.ok(mapToResponse(updatedReservation));
    }

    // Cancel reservation
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> cancelReservation(@PathVariable Long id) {
        TableReservation reservation = tableReservationService.getReservationById(id);

        // Check if the current user has permission to cancel this reservation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (reservation.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_MANAGER"))) {

            TableReservation cancelledReservation = tableReservationService.cancelReservation(id);
            return ResponseEntity.ok(mapToResponse(cancelledReservation));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Update reservation
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<TableReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody TableReservationRequest reservationRequest) {

        TableReservation reservation = tableReservationService.getReservationById(id);

        // Check if the current user has permission to update this reservation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        if (reservation.getUser().getUsername().equals(currentUsername) ||
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_MANAGER"))) {

            TableReservation updatedReservation = tableReservationService.updateReservation(id, reservationRequest);
            return ResponseEntity.ok(mapToResponse(updatedReservation));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Check table availability
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkTableAvailability(
            @RequestParam Long tableId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam(defaultValue = "120") int duration) {

        boolean isAvailable = tableReservationService.isTableAvailable(tableId, dateTime, duration);
        return ResponseEntity.ok(isAvailable);
    }

    // Helper method to map TableReservation entity to TableReservationResponse
    private TableReservationResponse mapToResponse(TableReservation reservation) {
        TableReservationResponse response = new TableReservationResponse();
        response.setId(reservation.getId());
        response.setTokenNumber(reservation.getTokenNumber());
        response.setUserId(reservation.getUser().getId());
        response.setUsername(reservation.getUser().getUsername());
        response.setTableId(reservation.getDiningTable().getId());
        response.setTableNumber(reservation.getDiningTable().getTableNumber());
        response.setDiningRoomId(reservation.getDiningTable().getDiningRoom().getId());
        response.setDiningRoomName(reservation.getDiningTable().getDiningRoom().getName());
        response.setRestaurantId(reservation.getDiningTable().getDiningRoom().getRestaurant().getId());
        response.setRestaurantName(reservation.getDiningTable().getDiningRoom().getRestaurant().getName());
        response.setReservationDateTime(reservation.getReservationDateTime());
        response.setNumberOfGuests(reservation.getNumberOfGuests());
        response.setStatus(reservation.getStatus());
        response.setSpecialRequests(reservation.getSpecialRequests());
        response.setCreatedAt(reservation.getCreatedAt());
        return response;
    }

    // Helper method to map list of entities to list of response objects
    private List<TableReservationResponse> mapToResponseList(List<TableReservation> reservations) {
        return reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
