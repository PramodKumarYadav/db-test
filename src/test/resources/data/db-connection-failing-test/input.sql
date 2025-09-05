
-- Create tables if not exist
CREATE TABLE source (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10));
CREATE TABLE target (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10));

-- Clean tables before each test to avoid PK violation in repeated tests
DELETE FROM source;
DELETE FROM target;

-- Insert sample data into source table
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (4, 'Emily', 'Clark', 22, 'Female');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (5, 'Michael', 'Johnson', 35, 'Male');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (6, 'Sarah', 'Lee', 27, 'Female');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (7, 'David', 'Kim', 40, 'Male');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (8, 'Anna', 'Martinez', 29, 'Female');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (9, 'Chris', 'Evans', 33, 'Male');
INSERT INTO source (id, first_name, last_name, age, gender) VALUES (10, 'Sophia', 'Turner', 24, 'Female');

-- Insert sample data into target table with some differences
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (1, 'Carl', 'Doe', 30, 'Male');
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female');
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Green', 29, 'Male'); 
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (4, 'Emily', 'Clark', 22, 'Female');
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (5, 'Michael', 'Johnson', 36, 'Male'); 
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (6, 'Sarah', 'Lee', 27, 'Female');
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (7, 'David', 'Kim', 41, 'Male'); 
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (8, 'Anna', 'Martinez', 29, 'Female');
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (9, 'Rock', 'Evans', 34, 'Male'); 
INSERT INTO target (id, first_name, last_name, age, gender) VALUES (10, 'Sophia', 'Turner', 24, 'Male'); 