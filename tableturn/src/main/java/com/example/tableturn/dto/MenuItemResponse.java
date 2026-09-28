package com.example.tableturn.dto;

import java.math.BigDecimal;

public record MenuItemResponse(Long id, String itemName, String description, BigDecimal unitPrice, boolean active) {
}
