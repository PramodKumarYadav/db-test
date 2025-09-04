
-- Create tables if not exist
CREATE TABLE emp (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10));
CREATE TABLE customer (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10));

-- Clean tables before each test to avoid PK violation in repeated tests
DELETE FROM emp;
DELETE FROM customer;

-- Insert 3 records in source table 'emp'
INSERT INTO emp (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male');
INSERT INTO emp (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female');
INSERT INTO emp (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male');

-- Insert same 3 records in target table 'customer'
INSERT INTO customer (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male');
INSERT INTO customer (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female');
INSERT INTO customer (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male');