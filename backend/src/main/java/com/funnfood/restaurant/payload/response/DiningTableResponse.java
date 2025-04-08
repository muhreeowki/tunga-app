package com.funnfood.restaurant.payload.response;

public class DiningTableResponse {

    private Long id;
    private String tableNumber;
    private int capacity;
    private Long diningRoomId;
    private String diningRoomName;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Long getDiningRoomId() {
        return diningRoomId;
    }

    public void setDiningRoomId(Long diningRoomId) {
        this.diningRoomId = diningRoomId;
    }

    public String getDiningRoomName() {
        return diningRoomName;
    }

    public void setDiningRoomName(String diningRoomName) {
        this.diningRoomName = diningRoomName;
    }
}
