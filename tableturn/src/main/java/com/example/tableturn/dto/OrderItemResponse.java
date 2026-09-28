package com.example.tableturn.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long sourceMenuItemId,
        String itemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
