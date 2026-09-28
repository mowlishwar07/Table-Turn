package com.example.tableturn.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotNull @Schema(example = "1") Long tableId,
        @NotBlank @Size(max = 100) @Schema(example = "Mowlishwar") String customerName,
        @NotEmpty List<@Valid OrderLineRequest> items) {
}
