package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.DiningTable;
import com.funnfood.restaurant.model.TableReservation;
import com.funnfood.restaurant.model.User;
import com.funnfood.restaurant.payload.request.TableReservationRequest;
import com.funnfood.restaurant.repository.DiningTableRepository;
import com.funnfood.restaurant.repository.TableReservationRepository;
import com.funnfood.restaurant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class TableReservationService {
    @Autowired
    private TableReservationRepository tableReservationRepository;

    @Autowired
    private DiningTableRepository diningTableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Get all reservations
     */
    @Transactional(readOnly = true)
    public List<TableReservation> getAllReservations() {
        return tableReservationRepository.findAll();
    }

    /**
     * Get reservation by ID
     */
    @Transactional(readOnly = true)
    public TableReservation getReservationById(Long id) {
        return tableReservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TableReservation", "id", id));
    }

    /**
     * Get reservations by user
     */
    @Transactional(readOnly = true)
    public List<TableReservation> getReservationsByUser(User user) {
        return tableReservationRepository.findByUser(user);
    }

    /**
     * Create a new reservation
     */
    @Transactional
    public TableReservation createReservation(User user, TableReservationRequest reservationRequest) {
        // Get the requested dining table
        DiningTable table = diningTableRepository.findById(reservationRequest.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", reservationRequest.getTableId()));

        // Check if table can accommodate the guests
        if (table.getCapacity() < reservationRequest.getNumberOfGuests()) {
            throw new IllegalArgumentException("Table capacity is less than the number of guests");
        }

        // Check for conflicting reservations
        LocalDateTime startTime = reservationRequest.getReservationDateTime().minusHours(1);
        LocalDateTime endTime = reservationRequest.getReservationDateTime().plusHours(1);
        List<TableReservation> conflictingReservations = tableReservationRepository.findConflictingReservations(
                table, startTime, endTime);

        if (!conflictingReservations.isEmpty()) {
            throw new IllegalStateException("Table is already reserved at this time");
        }

        // Create new reservation
        TableReservation reservation = new TableReservation();
        reservation.setUser(user);
        reservation.setDiningTable(table);
        reservation.setReservationDateTime(reservationRequest.getReservationDateTime());
        reservation.setNumberOfGuests(reservationRequest.getNumberOfGuests());
        reservation.setSpecialRequests(reservationRequest.getSpecialRequests());
        reservation.setTokenNumber(generateReservationToken());
        reservation.setStatus("CONFIRMED");
        reservation.setCreatedAt(LocalDateTime.now());

        TableReservation savedReservation = tableReservationRepository.save(reservation);

        // Send confirmation email
        sendConfirmationEmail(user, savedReservation, table);

        return savedReservation;
    }

    /**
     * Update reservation status
     */
    @Transactional
    public TableReservation updateReservationStatus(Long id, String status) {
        TableReservation reservation = getReservationById(id);
        reservation.setStatus(status);
        return tableReservationRepository.save(reservation);
    }

    /**
     * Cancel reservation
     */
    @Transactional
    public TableReservation cancelReservation(Long id) {
        TableReservation reservation = getReservationById(id);

        // Check if the reservation can be cancelled (not too close to the reserved time)
        if (reservation.getReservationDateTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalStateException("Reservations cannot be cancelled less than 2 hours before the reserved time");
        }

        reservation.setStatus("CANCELLED");

        // Send cancellation email


        return tableReservationRepository.save(reservation);
    }

    /**
     * Update reservation details
     */
    @Transactional
    public TableReservation updateReservation(Long id, TableReservationRequest reservationRequest) {
        TableReservation reservation = getReservationById(id);

        // Check if the reservation can be updated (not too close to the reserved time)
        if (reservation.getReservationDateTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalStateException("Reservations cannot be modified less than 2 hours before the reserved time");
        }

        // Check if table is changing
        if (!reservation.getDiningTable().getId().equals(reservationRequest.getTableId())) {
            DiningTable newTable = diningTableRepository.findById(reservationRequest.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", reservationRequest.getTableId()));

            // Check if new table can accommodate guests
            if (newTable.getCapacity() < reservationRequest.getNumberOfGuests()) {
                throw new IllegalArgumentException("Table capacity is less than the number of guests");
            }

            // Check for conflicting reservations on the new table
            LocalDateTime startTime = reservationRequest.getReservationDateTime().minusHours(1);
            LocalDateTime endTime = reservationRequest.getReservationDateTime().plusHours(1);
            List<TableReservation> conflictingReservations = tableReservationRepository.findConflictingReservations(
                    newTable, startTime, endTime);

            if (!conflictingReservations.isEmpty()) {
                throw new IllegalStateException("The requested table is already reserved at this time");
            }

            reservation.setDiningTable(newTable);
        }
        // If same table but different time, check for conflicts
        else if (!reservation.getReservationDateTime().equals(reservationRequest.getReservationDateTime())) {
            LocalDateTime startTime = reservationRequest.getReservationDateTime().minusHours(1);
            LocalDateTime endTime = reservationRequest.getReservationDateTime().plusHours(1);
            List<TableReservation> conflictingReservations = tableReservationRepository.findConflictingReservations(
                    reservation.getDiningTable(), startTime, endTime);

            // Filter out this reservation's ID from conflicts
            boolean hasConflict = conflictingReservations.stream()
                    .anyMatch(r -> !r.getId().equals(reservation.getId()));

            if (hasConflict) {
                throw new IllegalStateException("The requested time slot is already booked for this table");
            }
        }

        // Update reservation details
        reservation.setReservationDateTime(reservationRequest.getReservationDateTime());
        reservation.setNumberOfGuests(reservationRequest.getNumberOfGuests());
        reservation.setSpecialRequests(reservationRequest.getSpecialRequests());

        TableReservation updatedReservation = tableReservationRepository.save(reservation);

        // Send update confirmation email

        return updatedReservation;
    }

    /**
     * Generate unique reservation token
     */
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

    /**
     * Send reservation confirmation email
     */
    private void sendConfirmationEmail(User user, TableReservation reservation, DiningTable table) {
        String restaurantName = table.getDiningRoom().getRestaurant().getName();
        String formattedDate = reservation.getReservationDateTime().toLocalDate().toString();
        String formattedTime = reservation.getReservationDateTime().toLocalTime().toString();

        emailService.sendReservationConfirmation(
                user.getEmail(),
                user.getUsername(),
                restaurantName,
                formattedDate,
                formattedTime,
                reservation.getNumberOfGuests(),
                reservation.getTokenNumber());
    }

    @Transactional(readOnly = true)
    public boolean isTableAvailable(Long tableId, LocalDateTime dateTime, int duration) {
        DiningTable table = diningTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("DiningTable", "id", tableId));

        LocalDateTime startTime = dateTime.minusMinutes(30);  // Buffer before reservation
        LocalDateTime endTime = dateTime.plusMinutes(duration + 30);  // Reservation + buffer

        List<TableReservation> conflictingReservations = tableReservationRepository.findConflictingReservations(
                table, startTime, endTime);

        return conflictingReservations.isEmpty();
    }
}
