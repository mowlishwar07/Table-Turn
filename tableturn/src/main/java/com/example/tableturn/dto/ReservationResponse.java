package com.example.tableturn.dto;

import java.time.LocalDateTime;

import com.example.tableturn.model.ReservationStatus;
import com.example.tableturn.model.ReservationType;

public record ReservationResponse(
        Long id,
        String customerName,
        String phone,
        ReservationType type,
        Integer partySize,
        Long tableId,
        Integer tableNumber,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status) {
}
