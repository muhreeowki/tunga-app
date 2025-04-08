package com.funnfood.restaurant.repository;

import com.funnfood.restaurant.model.DiningTable;
import com.funnfood.restaurant.model.TableReservation;
import com.funnfood.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {
    List<TableReservation> findByUser(User user);

    @Query("SELECT r FROM TableReservation r WHERE r.diningTable = :table " +
            "AND ((r.reservationDateTime >= :startTime AND r.reservationDateTime <= :endTime) " +
            "OR (r.reservationDateTime <= :startTime AND FUNCTION('DATEADD', HOUR, 2, r.reservationDateTime) >= :startTime)) " +
            "AND r.status <> 'CANCELLED'")
    List<TableReservation> findConflictingReservations(
            @Param("table") DiningTable table,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
