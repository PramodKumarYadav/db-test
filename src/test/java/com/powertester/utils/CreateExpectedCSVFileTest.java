package com.powertester.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.powertester.database.DBConnection;

import com.powertester.extensions.TableCompareExtension;

@Slf4j
@ExtendWith(TableCompareExtension.class)
class CreateExpectedCSVFileTest {
    private static final DBConnection db = DBConnection.getInstance();

    @BeforeAll
    static void createTables() {
        // Create tables with more fields
        db.executeUpdate(
                "CREATE TABLE student (id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10))");

        // Clean tables before each test to avoid PK violation in repeated tests
        db.executeUpdate("DELETE FROM student");

        // Insert 3 records directly
        db.executeUpdate(
                "INSERT INTO student (id, first_name, last_name, age, gender) VALUES (1, 'John', 'Doe', 30, 'Male')");
        db.executeUpdate(
                "INSERT INTO student (id, first_name, last_name, age, gender) VALUES (2, 'Jane', 'Smith', 25, 'Female')");
        db.executeUpdate(
                "INSERT INTO student (id, first_name, last_name, age, gender) VALUES (3, 'Alex', 'Brown', 28, 'Male')");
    }

    @Test
    void generateExpectedCSVFileFromActualSQLOutput() throws java.io.IOException {
        // Arrange: Create expected results CSV based on actual SQL output. Later adjust the values as per requirements.
        List<Map<String, String>> actualRows = db.executePreparedStatement("SELECT * FROM student");

        // Generate expected csv file.
        CsvUtils.writeMapListToCsv("src/test/resources/testdata/expected_student_data.csv", actualRows);

        // Verify the generated CSV file
        List<Map<String, String>> expectedRows = CsvUtils.readCsvToMapList("src/test/resources/testdata/expected_student_data.csv");
        TableCompareExtension.captureRows(expectedRows, actualRows);

        // Completeness check: Assert that both input and output are of same size.
        assertEquals(expectedRows.size(), actualRows.size());
    }

    @AfterAll
    static void tearDownAll() {
        db.executeUpdate("DROP TABLE student");
        db.closeConnectionPool();
    }
}
