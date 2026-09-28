package com.example.tableturn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.tableturn.dto.CreateBillRequest;
import com.example.tableturn.dto.CreateOrderRequest;
import com.example.tableturn.dto.MenuItemRequest;
import com.example.tableturn.dto.OrderLineRequest;
import com.example.tableturn.dto.ReservationRequest;
import com.example.tableturn.dto.TableRequest;
import com.example.tableturn.model.ReservationType;
import com.example.tableturn.model.TableStatus;
import com.example.tableturn.service.TableTurnService;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tableturn-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TableturnApplicationTests {

    @Autowired
    private TableTurnService service;

    @Test
    @Transactional
    void walkInOrderBillAndVacateFlowWorks() {
        var table = service.createTable(new TableRequest(101, 4));
        var walkIn = service.createReservation(new ReservationRequest(
                table.id(), "Guest One", "9999999999", ReservationType.WALK_IN, 2, null, 120));
        assertEquals(TableStatus.OCCUPIED, service.getTableById(table.id()).status());

        var menu = service.createMenuItem(new MenuItemRequest("Test Meal", "Meal", new BigDecimal("125.00"), true));
        var order = service.createOrder(new CreateOrderRequest(table.id(), List.of(new OrderLineRequest(menu.id(), 2))));
        assertEquals(new BigDecimal("250.00"), order.total());

        var bill = service.generateBill(new CreateBillRequest(table.id()));
        assertEquals(new BigDecimal("250.00"), bill.total());
        assertEquals(walkIn.id(), bill.reservationId());
        assertEquals(TableStatus.BILLED, service.getTableById(table.id()).status());

        service.vacateTable(table.id());
        assertEquals(TableStatus.FREE, service.getTableById(table.id()).status());
    }

    @Test
    @Transactional
    void overlappingReservationsAreRejected() {
        var table = service.createTable(new TableRequest(102, 4));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        service.createReservation(new ReservationRequest(
                table.id(), "Guest A", null, ReservationType.RESERVE, 2, start, 120));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.createReservation(new ReservationRequest(
                        table.id(), "Guest B", null, ReservationType.RESERVE, 2, start.plusMinutes(30), 60)));
        assertEquals("The table already has a reservation during that time window", ex.getMessage());
    }
}
