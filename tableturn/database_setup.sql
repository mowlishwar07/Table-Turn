CREATE DATABASE IF NOT EXISTS db_tableturn;
USE db_tableturn;

-- Start the Spring Boot application after running this.
-- Hibernate/JPA creates exactly these 5 entity tables:
-- 1. restaurant_tables  -> restaurant table master + status
-- 2. reservations       -> WALK_IN / RESERVE customer bookings
-- 3. order_items        -> predefined MENU ITEMS ONLY
-- 4. restaurant_orders  -> ACTUAL CUSTOMER ORDERS (customer_name + order_details JSON + total_amount)
-- 5. bills              -> generated bill totals
--
-- IMPORTANT:
-- If you previously ran the older TableTurn build where ordered food rows were
-- also stored inside order_items, run reset_db_tableturn.sql ONCE before starting
-- this corrected version. This ensures the old schema does not remain.
