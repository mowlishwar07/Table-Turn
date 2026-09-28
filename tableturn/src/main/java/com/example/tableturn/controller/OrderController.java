package com.example.tableturn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tableturn.dto.CreateOrderRequest;
import com.example.tableturn.dto.OrderResponse;
import com.example.tableturn.service.TableTurnService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "4. Orders")
public class OrderController {

    private final TableTurnService service;

    public OrderController(TableTurnService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create order for occupied table", description = "Choose menu item IDs and quantities. The complete order snapshot and total are stored in restaurant_orders; order_items remains menu-only.")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "No parameters required. Click Try it out, then Execute.")
    public List<OrderResponse> getAll() {
        return service.getOrders();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id")
    public OrderResponse getById(@PathVariable Long id) {
        return service.getOrderById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an unbilled order")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
