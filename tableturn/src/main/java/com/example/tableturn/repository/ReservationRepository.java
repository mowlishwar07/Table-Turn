package com.example.tableturn.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tableturn.model.Reservation;
import com.example.tableturn.model.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByOrderByCreatedAtDesc();

    Optional<Reservation> findFirstByTableIdAndStatusOrderByCreatedAtDesc(Long tableId, ReservationStatus status);

    List<Reservation> findByTableIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long tableId,
            Collection<ReservationStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime);

    long countByTableId(Long tableId);
}
