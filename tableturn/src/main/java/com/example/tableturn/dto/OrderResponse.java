package com.example.tableturn.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long tableId,
        Integer tableNumber,
        Long reservationId,
        String customerName,
        LocalDateTime createdAt,
        Long billId,
        List<OrderItemResponse> items,
        BigDecimal total) {
}
