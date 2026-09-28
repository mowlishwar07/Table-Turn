package com.example.tableturn.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tableturn.dto.BillResponse;
import com.example.tableturn.dto.CreateBillRequest;
import com.example.tableturn.dto.CreateOrderRequest;
import com.example.tableturn.dto.MenuItemRequest;
import com.example.tableturn.dto.MenuItemResponse;
import com.example.tableturn.dto.OrderItemResponse;
import com.example.tableturn.dto.OrderResponse;
import com.example.tableturn.dto.ReservationRequest;
import com.example.tableturn.dto.ReservationResponse;
import com.example.tableturn.dto.TableRequest;
import com.example.tableturn.dto.TableResponse;
import com.example.tableturn.exception.ResourceNotFoundException;
import com.example.tableturn.model.Bill;
import com.example.tableturn.model.OrderItem;
import com.example.tableturn.model.Reservation;
import com.example.tableturn.model.ReservationStatus;
import com.example.tableturn.model.ReservationType;
import com.example.tableturn.model.RestaurantOrder;
import com.example.tableturn.model.RestaurantTable;
import com.example.tableturn.model.TableStatus;
import com.example.tableturn.repository.BillRepository;
import com.example.tableturn.repository.OrderItemRepository;
import com.example.tableturn.repository.ReservationRepository;
import com.example.tableturn.repository.RestaurantOrderRepository;
import com.example.tableturn.repository.TableRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TableTurnService {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BillRepository billRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TableTurnService(TableRepository tableRepository,
                            ReservationRepository reservationRepository,
                            RestaurantOrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            BillRepository billRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.billRepository = billRepository;
    }

    // -------------------- TABLE CRUD --------------------

    @Transactional
    public TableResponse createTable(TableRequest request) {
        ensureTableNumberAvailable(request.tableNumber(), null);
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(request.tableNumber());
        table.setCapacity(request.capacity());
        table.setStatus(TableStatus.FREE);
        return toTableResponse(tableRepository.save(table));
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getTables() {
        return tableRepository.findAllByOrderByTableNumberAsc().stream().map(this::toTableResponse).toList();
    }

    @Transactional(readOnly = true)
    public TableResponse getTableById(Long id) {
        return toTableResponse(getTable(id));
    }

    @Transactional
    public TableResponse updateTable(Long id, TableRequest request) {
        RestaurantTable table = getTable(id);
        if (table.getStatus() != TableStatus.FREE) {
            throw new IllegalStateException("Only a FREE table can be edited");
        }
        ensureTableNumberAvailable(request.tableNumber(), id);
        table.setTableNumber(request.tableNumber());
        table.setCapacity(request.capacity());
        return toTableResponse(tableRepository.save(table));
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = getTable(id);
        if (table.getStatus() != TableStatus.FREE) {
            throw new IllegalStateException("Only a FREE table can be deleted");
        }
        if (reservationRepository.countByTableId(id) > 0 || orderRepository.countByTableId(id) > 0 || billRepository.countByTableId(id) > 0) {
            throw new IllegalStateException("This table has history and cannot be deleted. Keep it for database history.");
        }
        tableRepository.delete(table);
    }

    // -------------------- RESERVATION --------------------

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        RestaurantTable table = getTable(request.tableId());
        requireFree(table);
        validatePartySize(table, request.partySize());

        Reservation reservation = new Reservation();
        reservation.setCustomerName(request.customerName().trim());
        reservation.setPhone(cleanNullable(request.phone()));
        reservation.setType(request.type());
        reservation.setTable(table);
        reservation.setPartySize(request.partySize());
        reservation.setCreatedAt(LocalDateTime.now());

        int duration = request.durationMinutes() == null ? 120 : request.durationMinutes();
        if (request.type() == ReservationType.WALK_IN) {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusMinutes(duration);
            ensureNoOverlap(table.getId(), start, end, null);
            reservation.setStartTime(start);
            reservation.setEndTime(end);
            reservation.setStatus(ReservationStatus.SEATED);
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        } else {
            if (request.startTime() == null) {
                throw new IllegalArgumentException("startTime is required when type is RESERVE");
            }
            LocalDateTime start = request.startTime();
            LocalDateTime end = start.plusMinutes(duration);
            ensureNoOverlap(table.getId(), start, end, null);
            reservation.setStartTime(start);
            reservation.setEndTime(end);
            reservation.setStatus(ReservationStatus.RESERVED);
        }

        return toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toReservationResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        return toReservationResponse(getReservation(id));
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        Reservation reservation = getReservation(id);
        if (reservation.getStatus() != ReservationStatus.RESERVED || reservation.getType() != ReservationType.RESERVE) {
            throw new IllegalStateException("Only a pending RESERVE reservation can be edited");
        }

        RestaurantTable table = getTable(request.tableId());
        requireFree(table);
        validatePartySize(table, request.partySize());
        if (request.type() != ReservationType.RESERVE) {
            throw new IllegalArgumentException("An existing reserved booking cannot be converted to WALK_IN");
        }
        if (request.startTime() == null) {
            throw new IllegalArgumentException("startTime is required when type is RESERVE");
        }

        int duration = request.durationMinutes() == null ? 120 : request.durationMinutes();
        LocalDateTime end = request.startTime().plusMinutes(duration);
        ensureNoOverlap(table.getId(), request.startTime(), end, reservation.getId());

        reservation.setCustomerName(request.customerName().trim());
        reservation.setPhone(cleanNullable(request.phone()));
        reservation.setTable(table);
        reservation.setPartySize(request.partySize());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(end);
        return toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse seatReservation(Long id) {
        Reservation reservation = getReservation(id);
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Only a RESERVED reservation can be seated");
        }
        RestaurantTable table = reservation.getTable();
        requireFree(table);
        table.setStatus(TableStatus.OCCUPIED);
        reservation.setStatus(ReservationStatus.SEATED);
        tableRepository.save(table);
        return toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = getReservation(id);
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Only a pending RESERVED reservation can be cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return toReservationResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservation(id);
        if (reservation.getStatus() == ReservationStatus.SEATED || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Seated/completed reservations are retained as history");
        }
        if (orderRepository.countByReservationId(id) > 0) {
            throw new IllegalStateException("A reservation with orders cannot be deleted");
        }
        reservationRepository.delete(reservation);
    }

    // -------------------- MENU / ORDER ITEM CRUD --------------------

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        OrderItem item = new OrderItem();
        item.setItemName(request.itemName().trim());
        item.setDescription(cleanNullable(request.description()));
        item.setUnitPrice(request.unitPrice().setScale(2, RoundingMode.HALF_UP));
        item.setActive(request.active() == null || request.active());
        return toMenuItemResponse(orderItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems() {
        return orderItemRepository.findAllByOrderByItemNameAsc().stream().map(this::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        return toMenuItemResponse(getMenuItem(id));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        OrderItem item = getMenuItem(id);
        item.setItemName(request.itemName().trim());
        item.setDescription(cleanNullable(request.description()));
        item.setUnitPrice(request.unitPrice().setScale(2, RoundingMode.HALF_UP));
        item.setActive(request.active() == null || request.active());
        return toMenuItemResponse(orderItemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        OrderItem item = getMenuItem(id);
        orderItemRepository.delete(item);
    }

    // -------------------- ORDERS --------------------

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        RestaurantTable table = getTable(request.tableId());
        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new IllegalStateException("Orders can only be placed for an OCCUPIED table");
        }
        Reservation activeReservation = reservationRepository
                .findFirstByTableIdAndStatusOrderByCreatedAtDesc(table.getId(), ReservationStatus.SEATED)
                .orElseThrow(() -> new IllegalStateException("No seated reservation/walk-in is active for this table"));

        RestaurantOrder order = new RestaurantOrder();
        order.setTable(table);
        order.setReservation(activeReservation);
        order.setCustomerName(request.customerName().trim());
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItemResponse> snapshots = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var line : request.items()) {
            OrderItem menu = getMenuItem(line.menuItemId());
            if (!menu.isActive()) {
                throw new IllegalStateException("Menu item '" + menu.getItemName() + "' is inactive");
            }

            BigDecimal lineTotal = menu.getUnitPrice()
                    .multiply(BigDecimal.valueOf(line.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            snapshots.add(new OrderItemResponse(
                    null,
                    menu.getId(),
                    menu.getItemName(),
                    line.quantity(),
                    menu.getUnitPrice(),
                    lineTotal));
            total = total.add(lineTotal);
        }

        order.setOrderDetails(writeOrderDetails(snapshots));
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return toOrderResponse(getOrder(id));
    }

    @Transactional
    public void deleteOrder(Long id) {
        RestaurantOrder order = getOrder(id);
        if (order.getBill() != null) {
            throw new IllegalStateException("A billed order cannot be deleted");
        }
        orderRepository.delete(order);
    }

    // -------------------- BILLING --------------------

    @Transactional
    public BillResponse generateBill(CreateBillRequest request) {
        RestaurantTable table = getTable(request.tableId());
        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new IllegalStateException("A bill can only be generated for an OCCUPIED table");
        }
        Reservation activeReservation = reservationRepository
                .findFirstByTableIdAndStatusOrderByCreatedAtDesc(table.getId(), ReservationStatus.SEATED)
                .orElseThrow(() -> new IllegalStateException("No active seated reservation exists for this table"));

        List<RestaurantOrder> orders = orderRepository
                .findAllByReservationIdAndBillIsNullOrderByCreatedAtAsc(activeReservation.getId());
        if (orders.isEmpty()) {
            throw new IllegalStateException("No unbilled orders exist for this table");
        }

        BigDecimal total = orders.stream()
                .map(RestaurantOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Bill bill = new Bill();
        bill.setTable(table);
        bill.setReservation(activeReservation);
        bill.setTotal(total);
        bill.setGeneratedAt(LocalDateTime.now());
        Bill savedBill = billRepository.save(bill);

        orders.forEach(order -> order.setBill(savedBill));
        orderRepository.saveAll(orders);
        table.setStatus(TableStatus.BILLED);
        tableRepository.save(table);

        return toBillResponse(savedBill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBills() {
        return billRepository.findAllByOrderByGeneratedAtDesc().stream().map(this::toBillResponse).toList();
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        return toBillResponse(getBill(id));
    }

    @Transactional
    public TableResponse vacateTable(Long tableId) {
        RestaurantTable table = getTable(tableId);
        if (table.getStatus() != TableStatus.BILLED) {
            throw new IllegalStateException("Only a BILLED table can be vacated");
        }
        Reservation reservation = reservationRepository
                .findFirstByTableIdAndStatusOrderByCreatedAtDesc(tableId, ReservationStatus.SEATED)
                .orElseThrow(() -> new IllegalStateException("No seated reservation is linked to this billed table"));
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        table.setStatus(TableStatus.FREE);
        return toTableResponse(tableRepository.save(table));
    }

    // -------------------- HELPERS --------------------

    private RestaurantTable getTable(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id " + id));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));
    }

    private RestaurantOrder getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    private OrderItem getMenuItem(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id " + id));
    }

    private Bill getBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + id));
    }

    private void requireFree(RestaurantTable table) {
        if (table.getStatus() != TableStatus.FREE) {
            throw new IllegalStateException("Table " + table.getTableNumber() + " must be FREE before assignment");
        }
    }

    private void validatePartySize(RestaurantTable table, Integer partySize) {
        if (partySize > table.getCapacity()) {
            throw new IllegalArgumentException("Party size exceeds table capacity of " + table.getCapacity());
        }
    }

    private void ensureNoOverlap(Long tableId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        boolean overlap = reservationRepository
                .findByTableIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        tableId,
                        List.of(ReservationStatus.RESERVED, ReservationStatus.SEATED),
                        end,
                        start)
                .stream()
                .anyMatch(existing -> excludeId == null || !existing.getId().equals(excludeId));
        if (overlap) {
            throw new IllegalStateException("The table already has a reservation during that time window");
        }
    }

    private void ensureTableNumberAvailable(Integer tableNumber, Long currentId) {
        tableRepository.findByTableNumber(tableNumber).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new IllegalStateException("Table number " + tableNumber + " already exists");
            }
        });
    }

    private String cleanNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String writeOrderDetails(List<OrderItemResponse> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to save order details", e);
        }
    }

    private List<OrderItemResponse> readOrderDetails(String orderDetails) {
        try {
            return objectMapper.readValue(orderDetails, new TypeReference<List<OrderItemResponse>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to read stored order details", e);
        }
    }

    private TableResponse toTableResponse(RestaurantTable table) {
        return new TableResponse(table.getId(), table.getTableNumber(), table.getCapacity(), table.getStatus());
    }

    private ReservationResponse toReservationResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getPhone(),
                reservation.getType(),
                reservation.getPartySize(),
                reservation.getTable().getId(),
                reservation.getTable().getTableNumber(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus());
    }

    private MenuItemResponse toMenuItemResponse(OrderItem item) {
        return new MenuItemResponse(item.getId(), item.getItemName(), item.getDescription(), item.getUnitPrice(), item.isActive());
    }

    private OrderResponse toOrderResponse(RestaurantOrder order) {
        List<OrderItemResponse> items = readOrderDetails(order.getOrderDetails());
        return new OrderResponse(
                order.getId(),
                order.getTable().getId(),
                order.getTable().getTableNumber(),
                order.getReservation().getId(),
                order.getCustomerName(),
                order.getCreatedAt(),
                order.getBill() == null ? null : order.getBill().getId(),
                items,
                order.getTotalAmount());
    }

    private BillResponse toBillResponse(Bill bill) {
        List<RestaurantOrder> orders = orderRepository.findAllByBillIdOrderByCreatedAtAsc(bill.getId());
        return new BillResponse(
                bill.getId(),
                bill.getTable().getId(),
                bill.getTable().getTableNumber(),
                bill.getReservation().getId(),
                bill.getReservation().getCustomerName(),
                bill.getGeneratedAt(),
                bill.getTotal(),
                orders.stream().map(this::toOrderResponse).toList());
    }
}
