package com.funnfood.restaurant.service;

import com.funnfood.restaurant.exception.ResourceNotFoundException;
import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.model.DiningTable;
import com.funnfood.restaurant.payload.request.DiningTableRequest;
import com.funnfood.restaurant.repository.DiningRoomRepository;
import com.funnfood.restaurant.repository.DiningTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiningTableServiceTest {

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private DiningRoomRepository diningRoomRepository;

    @InjectMocks
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
    void getAllTables_shouldReturnAllTables() {
        // Arrange
        List<DiningTable> expectedTables = Arrays.asList(diningTable);
        when(diningTableRepository.findAll()).thenReturn(expectedTables);

        // Act
        List<DiningTable> actualTables = diningTableService.getAllTables();

        // Assert
        assertEquals(expectedTables.size(), actualTables.size());
        assertEquals(expectedTables.get(0).getId(), actualTables.get(0).getId());
        verify(diningTableRepository, times(1)).findAll();
    }

    @Test
    void getTableById_withValidId_shouldReturnTable() {
        // Arrange
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(diningTable));

        // Act
        DiningTable actualTable = diningTableService.getTableById(1L);

        // Assert
        assertNotNull(actualTable);
        assertEquals(diningTable.getId(), actualTable.getId());
        verify(diningTableRepository, times(1)).findById(1L);
    }

    @Test
    void getTableById_withInvalidId_shouldThrowException() {
        // Arrange
        when(diningTableRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            diningTableService.getTableById(99L);
        });
        verify(diningTableRepository, times(1)).findById(99L);
    }

    @Test
    void getTablesByDiningRoom_shouldReturnTablesByDiningRoom() {
        // Arrange
        List<DiningTable> expectedTables = Arrays.asList(diningTable);
        when(diningRoomRepository.findById(1L)).thenReturn(Optional.of(diningRoom));
        when(diningTableRepository.findByDiningRoom(diningRoom)).thenReturn(expectedTables);

        // Act
        List<DiningTable> actualTables = diningTableService.getTablesByDiningRoom(1L);

        // Assert
        assertEquals(expectedTables.size(), actualTables.size());
        assertEquals(expectedTables.get(0).getId(), actualTables.get(0).getId());
        verify(diningRoomRepository, times(1)).findById(1L);
        verify(diningTableRepository, times(1)).findByDiningRoom(diningRoom);
    }

    @Test
    void createTable_shouldCreateAndReturnTable() {
        // Arrange
        when(diningRoomRepository.findById(1L)).thenReturn(Optional.of(diningRoom));
        when(diningTableRepository.save(any(DiningTable.class))).thenReturn(diningTable);

        // Act
        DiningTable createdTable = diningTableService.createTable(diningTableRequest);

        // Assert
        assertNotNull(createdTable);
        assertEquals(diningTable.getId(), createdTable.getId());
        assertEquals(diningTable.getTableNumber(), createdTable.getTableNumber());
        verify(diningRoomRepository, times(1)).findById(1L);
        verify(diningTableRepository, times(1)).save(any(DiningTable.class));
    }

    @Test
    void updateTable_shouldUpdateAndReturnTable() {
        // Arrange
        DiningTableRequest updateRequest = new DiningTableRequest();
        updateRequest.setTableNumber("T002");
        updateRequest.setCapacity(6);
        updateRequest.setDiningRoomId(1L);

        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(diningTable));
        when(diningRoomRepository.findById(1L)).thenReturn(Optional.of(diningRoom));
        when(diningTableRepository.save(any(DiningTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DiningTable updatedTable = diningTableService.updateTable(1L, updateRequest);

        // Assert
        assertNotNull(updatedTable);
        assertEquals("T002", updatedTable.getTableNumber());
        assertEquals(6, updatedTable.getCapacity());
        verify(diningTableRepository, times(1)).findById(1L);
        verify(diningRoomRepository, times(1)).findById(1L);
        verify(diningTableRepository, times(1)).save(any(DiningTable.class));
    }

    @Test
    void deleteTable_shouldDeleteTable() {
        // Arrange
        when(diningTableRepository.findById(1L)).thenReturn(Optional.of(diningTable));
        doNothing().when(diningTableRepository).delete(diningTable);

        // Act
        diningTableService.deleteTable(1L);

        // Assert
        verify(diningTableRepository, times(1)).findById(1L);
        verify(diningTableRepository, times(1)).delete(diningTable);
    }

    @Test
    void getAvailableTables_shouldReturnAvailableTables() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.now();
        List<DiningTable> expectedTables = Arrays.asList(diningTable);

        when(diningRoomRepository.findById(1L)).thenReturn(Optional.of(diningRoom));
        when(diningTableRepository.findAvailableTables(
                eq(diningRoom),
                eq(4),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(expectedTables);

        // Act
        List<DiningTable> actualTables = diningTableService.getAvailableTables(1L, 4, dateTime);

        // Assert
        assertEquals(expectedTables.size(), actualTables.size());
        assertEquals(expectedTables.get(0).getId(), actualTables.get(0).getId());
        verify(diningRoomRepository, times(1)).findById(1L);
        verify(diningTableRepository, times(1)).findAvailableTables(
                eq(diningRoom),
                eq(4),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }
}
