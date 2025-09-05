-- Create tables with more fields
CREATE TABLE student (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10));

-- Clean tables before each test to avoid PK violation in repeated tests
DELETE FROM student;

-- Insert 3 records directly
INSERT INTO student (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male');
INSERT INTO student (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female');
INSERT INTO student (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male');