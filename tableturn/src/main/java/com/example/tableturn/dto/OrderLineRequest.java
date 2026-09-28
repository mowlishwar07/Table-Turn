package com.example.tableturn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
        @NotNull @Schema(example = "1") Long menuItemId,
        @NotNull @Min(1) @Schema(example = "2") Integer quantity) {
}
