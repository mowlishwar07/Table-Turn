package com.example.tableturn.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuItemRequest(
        @NotBlank @Size(max = 120) @Schema(example = "Masala Dosa") String itemName,
        @Size(max = 255) @Schema(example = "Crispy dosa with potato masala") String description,
        @NotNull @DecimalMin(value = "0.01") @Schema(example = "75.00") BigDecimal unitPrice,
        @Schema(example = "true") Boolean active) {
}
