package com.powertester.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.powertester.extensions.TableCompareExtension;
import com.powertester.utils.CsvUtils;

@Slf4j
class DBConnectionPassingTest {
    private static final DBConnection db = DBConnection.getInstance();

    @BeforeAll
    static void createTables() {
        // Read and execute each SQL statement from input.sql
        String sqlFilePath = "src/test/resources/data/db-connection-passing-test/input.sql";
        db.updateFromFile(sqlFilePath);
    }

    // For a typical ETL scenario. Where input data is transformed and loaded into target system.
    @Test
    void compareOutputOfSQLStatementWithAExpectedCSVFile() throws java.io.IOException {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        String expectedCSVFilePath = "src/test/resources/data/db-connection-passing-test/expected.csv";
        List<Map<String, String>> expectedCustomers = CsvUtils.convertCsvToListOfMap(expectedCSVFilePath);

        String outputSQLFilePath = "src/test/resources/data/db-connection-passing-test/output.sql";
        List<Map<String, String>> actualCustomers = db.queryFromFile(outputSQLFilePath);

        // Completeness check: Assert that both input and output are of same size.
        assertEquals(expectedCustomers.size(), actualCustomers.size());

        // Correctness check: Assert that both input and output has same data.
        TableCompareExtension.captureRows(expectedCustomers, actualCustomers);
    }

    // For a typical EL scenario. Where input data is extracted and loaded (1:1) from source system(s) to target system.
    @Test
    void compareOutputOfTwoSQLStatements() {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        List<Map<String, String>> empRows = db.query("SELECT * FROM emp;");
        List<Map<String, String>> customerRows = db.query("SELECT * FROM customer;");

        // Completeness check: Assert that both input and output are of same size.
        assertEquals(empRows.size(), customerRows.size());

        // Correctness check: Assert that both input and output has same data.
        TableCompareExtension.captureRows(empRows, customerRows);
    }
    
    @AfterAll
    static void tearDownAll() {
        db.update("DROP TABLE emp;");
        db.update("DROP TABLE customer;");
    }
}
