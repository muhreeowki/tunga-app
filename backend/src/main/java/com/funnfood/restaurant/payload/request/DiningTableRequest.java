package com.funnfood.restaurant.payload.request;

import com.funnfood.restaurant.model.DiningTable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DiningTableRequest {

    @NotBlank(message = "Table number is required")
    private String tableNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Dining room ID is required")
    private Long diningRoomId;

    // Default constructor
    public DiningTableRequest() {
    }

    // Constructor with parameters
    public DiningTableRequest(String tableNumber, Integer capacity, Long diningRoomId) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.diningRoomId = diningRoomId;
    }

    // Getters and Setters
    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Long getDiningRoomId() {
        return diningRoomId;
    }

    public void setDiningRoomId(Long diningRoomId) {
        this.diningRoomId = diningRoomId;
    }
    // Convert to DiningTable entity
    public DiningTable toEntity() {
        DiningTable table = new DiningTable();
        table.setTableNumber(this.tableNumber);
        table.setCapacity(this.capacity);
        // DiningRoom will be set separately
        return table;
    }
}
