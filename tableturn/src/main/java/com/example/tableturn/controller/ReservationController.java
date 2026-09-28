package com.example.tableturn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tableturn.dto.ReservationRequest;
import com.example.tableturn.dto.ReservationResponse;
import com.example.tableturn.service.TableTurnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "2. Reservations")
public class ReservationController {

    private final TableTurnService service;

    public ReservationController(TableTurnService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Add reservation or walk-in", description = "Use type WALK_IN for immediate seating, or RESERVE for a future time slot.")
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return service.createReservation(request);
    }

    @GetMapping
    @Operation(summary = "Get all reservations", description = "No parameters required. Click Try it out, then Execute.")
    public List<ReservationResponse> getAll() {
        return service.getReservations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by id")
    public ReservationResponse getById(@PathVariable Long id) {
        return service.getReservationById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit pending reserved booking")
    public ReservationResponse update(@PathVariable Long id, @Valid @RequestBody ReservationRequest request) {
        return service.updateReservation(id, request);
    }

    @PostMapping("/{id}/seat")
    @Operation(summary = "Seat a reserved guest and occupy table")
    public ReservationResponse seat(@PathVariable Long id) {
        return service.seatReservation(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending reservation")
    public ReservationResponse cancel(@PathVariable Long id) {
        return service.cancelReservation(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pending/cancelled reservation")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
