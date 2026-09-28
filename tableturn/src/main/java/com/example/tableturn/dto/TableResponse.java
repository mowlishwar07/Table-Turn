package com.example.tableturn.dto;

import com.example.tableturn.model.TableStatus;

public record TableResponse(Long id, Integer tableNumber, Integer capacity, TableStatus status) {
}
