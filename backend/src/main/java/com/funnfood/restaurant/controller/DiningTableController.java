package com.funnfood.restaurant.controller;

import com.funnfood.restaurant.model.DiningTable;
import com.funnfood.restaurant.payload.request.DiningTableRequest;
import com.funnfood.restaurant.payload.response.DiningTableResponse;
import com.funnfood.restaurant.payload.response.MessageResponse;
import com.funnfood.restaurant.service.DiningTableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dining-tables")
public class DiningTableController {

    @Autowired
    private DiningTableService diningTableService;

    @GetMapping
    public ResponseEntity<List<DiningTableResponse>> getAllTables() {
        List<DiningTable> tables = diningTableService.getAllTables();
        List<DiningTableResponse> responseList = tables.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiningTableResponse> getTableById(@PathVariable Long id) {
        DiningTable table = diningTableService.getTableById(id);
        return ResponseEntity.ok(mapToResponse(table));
    }

    @GetMapping("/dining-room/{diningRoomId}")
    public ResponseEntity<List<DiningTableResponse>> getTablesByDiningRoom(@PathVariable Long diningRoomId) {
        List<DiningTable> tables = diningTableService.getTablesByDiningRoom(diningRoomId);
        List<DiningTableResponse> responseList = tables.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DiningTableResponse>> getAvailableTables(
            @RequestParam Long diningRoomId,
            @RequestParam int capacity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {

        List<DiningTable> tables = diningTableService.getAvailableTables(diningRoomId, capacity, dateTime);
        List<DiningTableResponse> responseList = tables.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DiningTableResponse> createTable(@Valid @RequestBody DiningTableRequest tableRequest) {
        DiningTable table = diningTableService.createTable(tableRequest);
        return new ResponseEntity<>(mapToResponse(table), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DiningTableResponse> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody DiningTableRequest tableRequest) {

        DiningTable table = diningTableService.updateTable(id, tableRequest);
        return ResponseEntity.ok(mapToResponse(table));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {
        diningTableService.deleteTable(id);
        return ResponseEntity.ok(new MessageResponse("Dining table deleted successfully"));
    }

    // Helper method to map DiningTable entity to DiningTableResponse
    private DiningTableResponse mapToResponse(DiningTable table) {
        DiningTableResponse response = new DiningTableResponse();
        response.setId(table.getId());
        response.setTableNumber(table.getTableNumber());
        response.setCapacity(table.getCapacity());

        if (table.getDiningRoom() != null) {
            response.setDiningRoomId(table.getDiningRoom().getId());
            response.setDiningRoomName(table.getDiningRoom().getName());
        }

        return response;
    }
}
