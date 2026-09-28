package com.example.tableturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TableRequest(
        @NotNull @Min(1) @Schema(example = "1") Integer tableNumber,
        @NotNull @Min(1) @Schema(example = "4") Integer capacity) {
}
