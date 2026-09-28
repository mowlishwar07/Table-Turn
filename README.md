# TableTurn — Restaurant Table Reservation and Order Tracker

TableTurn is a Java Spring Boot + MySQL web application developed to manage restaurant tables, reservations, walk-in customers, menu items, customer orders, billing, and table availability. The application replaces manual table reservation and paper-based order tracking with a centralized web-based system.

## Project Details

Project Name: TableTurn  
Base Package: com.example.tableturn  
Database Name: tableturn_db

## Problem Statement

Restaurants often manage table reservations over phone calls and record customer orders manually. During busy hours, this can lead to double booking of restaurant tables, difficulty tracking occupied and available tables, confusion between reservations and walk-in customers, incorrect order tracking, billing mistakes, and difficulty maintaining menu prices.

TableTurn provides a Spring Boot web application to manage the complete restaurant workflow from table reservation to billing.

## Main Features

### 1. Table Management

The application allows restaurant staff to add, view, search, edit, and delete restaurant tables. Each table stores its table number, seating capacity, and current status.

Table status is automatically maintained as:

FREE → OCCUPIED → BILLED → FREE

### 2. Reservation Management

TableTurn supports two reservation types:

- RESERVE
- WALK_IN

Reservation details include:

- Customer name
- Phone number
- Reservation type
- Party size
- Restaurant table
- Reservation date and time

The system prevents overlapping reservations for the same restaurant table.

For a walk-in customer, the selected table is immediately marked as OCCUPIED.

A reserved customer can later be seated using the Seat option.

### 3. Menu / Order Item Management

The order_items table is used as the predefined restaurant menu.

Each menu item contains:

- Item ID
- Item name
- Description
- Unit price
- Active status

Restaurant staff can:

- Add menu items
- View menu items
- Edit item details
- Update prices
- Delete menu items

The order_items table stores menu items only and does not directly store customer orders.

### 4. Order Management

Actual customer orders are stored in the restaurant_orders table.

Each order contains:

- Order ID
- Restaurant table
- Reservation reference
- Customer name
- Selected menu items
- Item quantities
- Order details
- Total amount
- Bill reference
- Created time

The customer selects items from the predefined menu. The application stores a snapshot of the selected item details in the actual restaurant order.

This ensures that changing a menu item's price later does not modify previously placed orders.

Orders can only be placed for occupied tables.

### 5. Billing Management

The system generates a bill for the customer's current table session.

The bill amount is calculated using the total amount of all unbilled restaurant orders associated with that table.

Bill information includes:

- Bill ID
- Restaurant table
- Total bill amount
- Billing date and time

Bills can also be searched using the Bill ID.

After billing, the table can be vacated and its status is changed back to FREE.

## Database Design

The project contains exactly five main entities:

1. RestaurantTable → restaurant_tables  
   Stores restaurant table information and status.

2. Reservation → reservations  
   Stores reserved and walk-in customer information.

3. OrderItem → order_items  
   Stores predefined menu items.

4. RestaurantOrder → restaurant_orders  
   Stores actual customer orders.

5. Bill → bills  
   Stores final billing details.

## Basic Application Flow

Restaurant Table  
↓  
Reservation / Walk-In  
↓  
Table becomes OCCUPIED  
↓  
Select Menu Items  
↓  
Create Restaurant Order  
↓  
Order stored in restaurant_orders  
↓  
Generate Bill  
↓  
Table marked BILLED  
↓  
Vacate Table  
↓  
Table becomes FREE

## Technology Stack

### Frontend

- HTML5
- CSS3
- JavaScript

### Backend

- Java 17
- Spring Boot
- Spring MVC
- REST API

### Database

- MySQL
- Spring Data JPA
- Hibernate
- MySQL Connector/J

### Validation and API Documentation

- Jakarta Validation
- Swagger / OpenAPI
- Springdoc OpenAPI

### Build Tool

- Maven

## User Interface

The application uses a tab-based user interface.

The main tabs are:

- Tables
- Reservations
- Menu / Order Items
- Orders
- Bills

This allows restaurant staff to move between different modules easily while using a single application interface.

## Business Rules

1. A restaurant table must be free before it can be assigned to a new walk-in customer.

2. The system prevents overlapping reservations for the same table.

3. A customer order can only be created for an occupied table.

4. Menu items are stored separately from actual restaurant orders.

5. The order total is automatically calculated using the selected menu items and quantities.

6. The bill total is calculated from the actual restaurant orders.

7. A table becomes available again only after billing and vacating.

8. Invalid requests return clear error messages instead of storing incorrect data.

## MySQL Database Setup

Create the database in MySQL Workbench using:

CREATE DATABASE tableturn_db;
USE tableturn_db;

## Database Configuration

Open:

src/main/resources/application.properties

Configure the MySQL connection:

spring.datasource.url=jdbc:mysql://localhost:3306/tableturn_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

Replace YOUR_MYSQL_PASSWORD with your MySQL password.

## Running the Project

Open the project folder in VS Code.

Open the terminal and run:

mvn spring-boot:run

After Spring Boot starts successfully, open:

http://localhost:8080

## Swagger API Documentation

Swagger UI is available at:

http://localhost:8080/swagger-ui.html

Swagger can be used to test the REST API operations.

Main API endpoints include:

GET /api/tables
POST /api/tables
GET /api/tables/{id}
PUT /api/tables/{id}
DELETE /api/tables/{id}

GET /api/reservations
POST /api/reservations

GET /api/menu-items
POST /api/menu-items

GET /api/orders
POST /api/orders

GET /api/bills
GET /api/bills/{id}

For endpoints such as:

GET /api/tables
GET /api/reservations
GET /api/menu-items
GET /api/orders
GET /api/bills

no input parameter is required.

In Swagger, simply click:

Try it out → Execute

## Validation

The project performs validation before saving data.

Validation includes:

- Required fields cannot be empty
- Capacity and party size must contain valid values
- Invalid table assignments are rejected
- Double booking is prevented
- Orders cannot be placed for unavailable tables
- Invalid IDs return clear error responses


## Conclusion

TableTurn successfully provides a complete restaurant table reservation and order tracking system using Java Spring Boot and MySQL.

The application manages restaurant tables, reserved and walk-in customers, predefined menu items, actual customer orders, billing, and table availability through a simple tab-based web interface.

The project also includes REST APIs, validation, MySQL database integration, CRUD operations, Swagger testing, business-rule enforcement, and automatic table-status management.
