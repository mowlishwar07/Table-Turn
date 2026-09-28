-- DEVELOPMENT RESET FOR THE CORRECTED TABLETURN SCHEMA
-- WARNING: This deletes existing db_tableturn data.
DROP DATABASE IF EXISTS db_tableturn;
CREATE DATABASE db_tableturn;
USE db_tableturn;

-- Now start the Spring Boot application.
-- JPA/Hibernate will create the corrected 5-table schema automatically.
