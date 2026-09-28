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

import com.example.tableturn.dto.MenuItemRequest;
import com.example.tableturn.dto.MenuItemResponse;
import com.example.tableturn.service.TableTurnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menu-items")
@Tag(name = "3. Menu / Order Items")
public class MenuItemController {

    private final TableTurnService service;

    public MenuItemController(TableTurnService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create menu item", description = "Stored in order_items as the predefined menu catalog. Actual customer orders are stored in restaurant_orders.")
    public MenuItemResponse create(@Valid @RequestBody MenuItemRequest request) {
        return service.createMenuItem(request);
    }

    @GetMapping
    @Operation(summary = "Get all menu items", description = "No parameters required. Click Try it out, then Execute.")
    public List<MenuItemResponse> getAll() {
        return service.getMenuItems();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by id")
    public MenuItemResponse getById(@PathVariable Long id) {
        return service.getMenuItemById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit menu item or price")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return service.updateMenuItem(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu item")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
