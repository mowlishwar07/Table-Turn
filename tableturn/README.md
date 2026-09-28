# TableTurn

**TableTurn — Restaurant Table Reservation and Order Tracker**

- Project name: `tableturn`
- Base package: `com.example.tableturn`
- Database: `db_tableturn`
- Java: 17
- Spring Boot: 4.1.1
- MySQL + Spring Data JPA + Jakarta Validation + Swagger/OpenAPI

## Exactly 5 entities / database tables

1. `restaurant_tables` — physical restaurant tables and `FREE / OCCUPIED / BILLED` status.
2. `reservations` — both `WALK_IN` and `RESERVE` customer bookings.
3. `order_items` — **predefined menu items only** (name, description, unit price, active status).
4. `restaurant_orders` — **actual customer orders** for a seated table. Each row stores `customer_name`, the selected menu-item snapshot in `order_details`, and the calculated `total_amount`.
5. `bills` — final total for all unbilled `restaurant_orders` in the current sitting.

## Correct order data flow

`order_items (menu)` → user selects items/quantities → `restaurant_orders (actual order)` → `bills`

`order_items` is never used to create customer-order rows. It remains the editable menu catalog.

When an order is created:
- the customer name is saved directly in `restaurant_orders.customer_name`;
- the selected menu name, quantity and current price are copied into `restaurant_orders.order_details` as JSON;
- the order total is saved in `restaurant_orders.total_amount`;
- later menu-price edits do not alter old orders or old bills.

## Main flow

1. Create restaurant tables.
2. Add a reservation.
   - `WALK_IN` immediately makes the selected FREE table OCCUPIED.
   - `RESERVE` stores a future time slot and prevents overlap.
3. Seat a `RESERVE` booking when the guest arrives.
4. Take an order by selecting predefined records from `order_items`.
5. The actual order is saved in `restaurant_orders`.
6. Generate a bill. The bill total is the sum of `restaurant_orders.total_amount` for the current sitting.
7. Vacate the billed table to make it FREE again.

## Corrected database note

If you already ran the older TableTurn version, its `order_items` table may still contain old order-snapshot columns/rows because Hibernate `ddl-auto=update` does not remove obsolete columns.

For this college project, the cleanest fix is to run **`reset_db_tableturn.sql` once** in MySQL Workbench, then start this corrected project. This recreates `db_tableturn` with the corrected entity schema.

## Run with MySQL

1. Start MySQL.
2. For a new database, run `database_setup.sql`.
3. If upgrading from the older TableTurn build, run `reset_db_tableturn.sql` once instead.
4. Check `src/main/resources/application.properties`.
   - Default username: `root`
   - Default password: `root2008`
   - Change it if your MySQL password is different.
5. Open a terminal in the project folder and run:

```bat
mvn spring-boot:run
```

or on Windows:

```bat
mvnw.cmd spring-boot:run
```

## URLs

- Main UI: `http://localhost:8080/`
- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Swagger quick GET endpoints

These require **no parameters**. Open the endpoint, click **Try it out**, then **Execute**:

- `GET /api/tables`
- `GET /api/reservations`
- `GET /api/menu-items`
- `GET /api/orders`
- `GET /api/bills`

`GET /api/bills/{id}` retrieves one bill using its ID.

## Business rules enforced in service layer

- A table must be FREE before a walk-in is seated or a reservation is assigned.
- Party size cannot exceed table capacity.
- Overlapping reservation windows for the same table are rejected.
- Orders can only be created for an OCCUPIED table.
- Only active menu items can be ordered.
- Actual customer orders are saved in `restaurant_orders`, not `order_items`.
- Bill total is calculated from stored restaurant order totals.
- A billed table must be vacated before it becomes FREE.
- Invalid requests return clear JSON errors through `@RestControllerAdvice`.

## UI tabs

The main page uses tabs for **Tables**, **Reservations**, **Menu / Order Items**, **Orders**, and **Bills**, so only one module is shown at a time. The Order form also includes a customer name field and auto-fills it from the currently seated reservation when possible.
