package com.example.tableturn.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "TableTurn REST API",
                version = "1.0",
                description = "Restaurant table reservation, walk-in seating, menu/order tracking and billing API."),
        tags = {
                @Tag(name = "1. Tables", description = "CRUD operations for restaurant tables."),
                @Tag(name = "2. Reservations", description = "Create RESERVE or WALK_IN reservations and seat/cancel them."),
                @Tag(name = "3. Menu / Order Items", description = "CRUD operations for predefined menu items stored in order_items."),
                @Tag(name = "4. Orders", description = "Create and track orders for occupied tables."),
                @Tag(name = "5. Bills", description = "Generate bills, fetch bills by id, and vacate billed tables.")
        })
public class OpenApiConfiguration {
}
