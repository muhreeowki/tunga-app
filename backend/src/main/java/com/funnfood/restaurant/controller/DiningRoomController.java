package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.service.DiningRoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dining-rooms")
public class DiningRoomController {
    @Autowired
    private DiningRoomService diningRoomService;

    @GetMapping
    public ResponseEntity<List<DiningRoom>> getAllDiningRooms() {
        List<DiningRoom> diningRooms = diningRoomService.getAllDiningRooms();
        return ResponseEntity.ok(diningRooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiningRoom> getDiningRoomById(@PathVariable Long id) {
        DiningRoom diningRoom = diningRoomService.getDiningRoomById(id);
        return ResponseEntity.ok(diningRoom);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DiningRoom>> getAvailableDiningRooms() {
        List<DiningRoom> diningRooms = diningRoomService.getAvailableDiningRooms();
        return ResponseEntity.ok(diningRooms);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DiningRoom> createDiningRoom(@Valid @RequestBody DiningRoom diningRoom) {
        DiningRoom newDiningRoom = diningRoomService.createDiningRoom(diningRoom);
        return new ResponseEntity<>(newDiningRoom, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DiningRoom> updateDiningRoom(
            @PathVariable Long id,
            @Valid @RequestBody DiningRoom diningRoom) {
        DiningRoom updatedDiningRoom = diningRoomService.updateDiningRoom(id, diningRoom);
        return ResponseEntity.ok(updatedDiningRoom);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDiningRoom(@PathVariable Long id) {
        diningRoomService.deleteDiningRoom(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DiningRoom> updateDiningRoomStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        DiningRoom updatedDiningRoom = diningRoomService.updateStatus(id, status);
        return ResponseEntity.ok(updatedDiningRoom);
    }
}
