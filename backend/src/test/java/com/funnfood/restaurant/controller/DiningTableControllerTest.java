package com.funnfood.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.model.DiningTable;
import com.funnfood.restaurant.payload.request.DiningTableRequest;
import com.funnfood.restaurant.payload.response.DiningTableResponse;
import com.funnfood.restaurant.service.DiningTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DiningTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private DiningTableService diningTableService;

    private DiningTable diningTable;
    private DiningRoom diningRoom;
    private DiningTableRequest diningTableRequest;

    @BeforeEach
    void setUp() {
        // Set up test data
        diningRoom = new DiningRoom();
        diningRoom.setId(1L);
        diningRoom.setName("Main Dining");
        diningRoom.setStatus("ACTIVE");

        diningTable = new DiningTable();
        diningTable.setId(1L);
        diningTable.setTableNumber("T001");
        diningTable.setCapacity(4);
        diningTable.setDiningRoom(diningRoom);

        diningTableRequest = new DiningTableRequest();
        diningTableRequest.setTableNumber("T001");
        diningTableRequest.setCapacity(4);
        diningTableRequest.setDiningRoomId(1L);
    }

    @Test
    public void getAllTables_shouldReturnAllTables() throws Exception {
        // Arrange
        List<DiningTable> tables = Arrays.asList(diningTable);
        when(diningTableService.getAllTables()).thenReturn(tables);

        // Act & Assert
        mockMvc.perform(get("/api/dining-tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tableNumber", is("T001")))
                .andExpect(jsonPath("$[0].capacity", is(4)))
                .andExpect(jsonPath("$[0].diningRoomId", is(1)))
                .andExpect(jsonPath("$[0].diningRoomName", is("Main Dining")));

        verify(diningTableService, times(1)).getAllTables();
    }

    @Test
    public void getTableById_shouldReturnTable() throws Exception {
        // Arrange
        when(diningTableService.getTableById(1L)).thenReturn(diningTable);

        // Act & Assert
        mockMvc.perform(get("/api/dining-tables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber", is("T001")))
                .andExpect(jsonPath("$.capacity", is(4)))
                .andExpect(jsonPath("$.diningRoomId", is(1)))
                .andExpect(jsonPath("$.diningRoomName", is("Main Dining")));

        verify(diningTableService, times(1)).getTableById(1L);
    }

    @Test
    public void getTablesByDiningRoom_shouldReturnTables() throws Exception {
        // Arrange
        List<DiningTable> tables = Arrays.asList(diningTable);
        when(diningTableService.getTablesByDiningRoom(1L)).thenReturn(tables);

        // Act & Assert
        mockMvc.perform(get("/api/dining-tables/dining-room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tableNumber", is("T001")))
                .andExpect(jsonPath("$[0].capacity", is(4)));

        verify(diningTableService, times(1)).getTablesByDiningRoom(1L);
    }

    @Test
    public void getAvailableTables_shouldReturnAvailableTables() throws Exception {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2025, 4, 10, 18, 0);
        List<DiningTable> tables = Arrays.asList(diningTable);
        when(diningTableService.getAvailableTables(eq(1L), eq(4), any(LocalDateTime.class))).thenReturn(tables);

        // Act & Assert
        mockMvc.perform(get("/api/dining-tables/available")
                        .param("diningRoomId", "1")
                        .param("capacity", "4")
                        .param("dateTime", "2025-04-10T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tableNumber", is("T001")))
                .andExpect(jsonPath("$[0].capacity", is(4)));

        verify(diningTableService, times(1)).getAvailableTables(eq(1L), eq(4), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createTable_shouldCreateAndReturnTable() throws Exception {
        // Arrange
        when(diningTableService.createTable(any(DiningTableRequest.class))).thenReturn(diningTable);

        // Act & Assert
        mockMvc.perform(post("/api/dining-tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diningTableRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber", is("T001")))
                .andExpect(jsonPath("$.capacity", is(4)));

        verify(diningTableService, times(1)).createTable(any(DiningTableRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateTable_shouldUpdateAndReturnTable() throws Exception {
        // Arrange
        when(diningTableService.updateTable(eq(1L), any(DiningTableRequest.class))).thenReturn(diningTable);

        // Act & Assert
        mockMvc.perform(put("/api/dining-tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diningTableRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber", is("T001")))
                .andExpect(jsonPath("$.capacity", is(4)));

        verify(diningTableService, times(1)).updateTable(eq(1L), any(DiningTableRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteTable_shouldDeleteTable() throws Exception {
        // Arrange
        doNothing().when(diningTableService).deleteTable(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/dining-tables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted successfully")));

        verify(diningTableService, times(1)).deleteTable(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void createTable_withInsufficientRole_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/dining-tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diningTableRequest)))
                .andExpect(status().isForbidden());

        verify(diningTableService, never()).createTable(any(DiningTableRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updateTable_withInsufficientRole_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/dining-tables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diningTableRequest)))
                .andExpect(status().isForbidden());

        verify(diningTableService, never()).updateTable(anyLong(), any(DiningTableRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void deleteTable_withInsufficientRole_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/dining-tables/1"))
                .andExpect(status().isForbidden());

        verify(diningTableService, never()).deleteTable(anyLong());
    }
}
