package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.DiningRoom;
import com.funnfood.restaurant.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findByDiningRoom(DiningRoom diningRoom);
    boolean existsByTableNumberAndDiningRoomId(String tableNumber, Long diningRoomId);


    @Query("SELECT t FROM DiningTable t WHERE t.diningRoom = :diningRoom AND t.capacity >= :capacity " +
            "AND NOT EXISTS (SELECT r FROM TableReservation r WHERE r.diningTable = t " +
            "AND ((r.reservationDateTime >= :startTime AND r.reservationDateTime <= :endTime) " +
            "OR (r.reservationDateTime <= :startTime AND FUNCTION('TIMESTAMPADD', HOUR, 2, r.reservationDateTime) >= :startTime)) " +
            "AND r.status <> 'CANCELLED')")
    List<DiningTable> findAvailableTables(
            @Param("diningRoom") DiningRoom diningRoom,
            @Param("capacity") int capacity,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
