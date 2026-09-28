package com.example.tableturn.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BillResponse(
        Long id,
        Long tableId,
        Integer tableNumber,
        Long reservationId,
        String customerName,
        LocalDateTime generatedAt,
        BigDecimal total,
        List<OrderResponse> orders) {
}
