package com.example.tableturn.dto;

import java.time.LocalDateTime;

import com.example.tableturn.model.ReservationType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReservationRequest(
        @NotNull @Schema(example = "1") Long tableId,
        @NotBlank @Size(max = 100) @Schema(example = "Arun Kumar") String customerName,
        @Size(max = 30) @Schema(example = "9876543210") String phone,
        @NotNull @Schema(example = "WALK_IN", allowableValues = {"WALK_IN", "RESERVE"}) ReservationType type,
        @NotNull @Min(1) @Schema(example = "3") Integer partySize,
        @Schema(description = "Required for RESERVE. Ignored for WALK_IN.", example = "2026-09-29T19:00:00") LocalDateTime startTime,
        @Min(30) @Schema(description = "Minutes. Defaults to 120.", example = "120") Integer durationMinutes) {
}
