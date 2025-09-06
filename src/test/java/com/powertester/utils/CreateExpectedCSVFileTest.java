package com.powertester.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.powertester.database.DBConnection;

import com.powertester.extensions.TableCompareExtension;

@Slf4j
class CreateExpectedCSVFileTest {
    private static final DBConnection db = DBConnection.getInstance();

    @BeforeAll
    static void createTables() {
        // Read and execute each SQL statement from input.sql
        String sqlFilePath = "src/test/resources/data/create-expected-csv-file-test/input.sql";
        db.updateFromFile(sqlFilePath);
    }

    @Test
    void generateExpectedCSVFileFromActualSQLOutput() throws java.io.IOException {
        // Arrange: Create expected results CSV based on actual SQL output. Later adjust the values as per requirements.
        String outputSQLFilePath = "src/test/resources/data/create-expected-csv-file-test/output.sql";
        List<Map<String, String>> actualRows = db.queryFromFile(outputSQLFilePath);

        // Generate expected csv file.
        String expectedCSVFilePath = "src/test/resources/data/create-expected-csv-file-test/expected.csv";
        CsvUtils.saveDataToCsvFile(expectedCSVFilePath, actualRows);

        // Get the generated CSV file
        List<Map<String, String>> expectedRows = CsvUtils.convertCsvToListOfMap(expectedCSVFilePath);
        TableCompareExtension.captureRows(expectedRows, actualRows);

        // Completeness check: Assert that both input and output are of same size.
        assertEquals(expectedRows.size(), actualRows.size());
    }
    
    @AfterAll
    static void tearDownAll() {
        db.update("DROP TABLE student;");
    }
}
