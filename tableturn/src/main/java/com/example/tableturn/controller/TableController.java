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

import com.example.tableturn.dto.TableRequest;
import com.example.tableturn.dto.TableResponse;
import com.example.tableturn.service.TableTurnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "1. Tables")
public class TableController {

    private final TableTurnService service;

    public TableController(TableTurnService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create table")
    public TableResponse create(@Valid @RequestBody TableRequest request) {
        return service.createTable(request);
    }

    @GetMapping
    @Operation(summary = "Get all tables", description = "No parameters required. Click Try it out, then Execute.")
    public List<TableResponse> getAll() {
        return service.getTables();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get table by id")
    public TableResponse getById(@PathVariable Long id) {
        return service.getTableById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit table")
    public TableResponse update(@PathVariable Long id, @Valid @RequestBody TableRequest request) {
        return service.updateTable(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete unused free table")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}
