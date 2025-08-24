package com.powertester.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.powertester.extensions.TableCompareExtension;

@Slf4j
@ExtendWith(TableCompareExtension.class)
class DBConnectionPassingTest {
    private static final DBConnection db = DBConnection.getInstance();

    @BeforeAll
    static void createTables() {
        // Create tables with more fields
        db.executeUpdate(
                "CREATE TABLE emp (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10))");
        db.executeUpdate(
                "CREATE TABLE customer (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10))");

        // Clean tables before each test to avoid PK violation in repeated tests
        db.executeUpdate("DELETE FROM emp");
        db.executeUpdate("DELETE FROM customer");

        // Insert 3 records directly
        db.executeUpdate(
                "INSERT INTO emp (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male')");
        db.executeUpdate(
                "INSERT INTO emp (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female')");
        db.executeUpdate(
                "INSERT INTO emp (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male')");

        db.executeUpdate(
                "INSERT INTO customer (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male')");
        db.executeUpdate(
                "INSERT INTO customer (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female')");
        db.executeUpdate(
                "INSERT INTO customer (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male')");
    }

    @Test
    void testCompareEmpAndCustomerTables() {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        List<Map<String, String>> empRows = db.executePreparedStatement("SELECT * FROM emp");
        List<Map<String, String>> customerRows = db.executePreparedStatement("SELECT * FROM customer");

        // Completeness check: Assert that both input and output are of same size.
        assertEquals(empRows.size(), customerRows.size());

        // Correctness check: Assert that both input and output has same data.
        TableCompareExtension.captureRows(empRows, customerRows);
    }

    @AfterAll
    static void tearDownAll() {
        db.executeUpdate("DROP TABLE emp");
        db.executeUpdate("DROP TABLE customer");
        db.closeConnectionPool();
    }
}
