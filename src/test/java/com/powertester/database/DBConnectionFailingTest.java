package com.powertester.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import com.powertester.extensions.TableCompareExtension;
import com.powertester.utils.CsvUtils;

@Slf4j
class DBConnectionFailingTest {
    private static final DBConnection db = DBConnection.getInstance();

    @BeforeAll
    static void createTables() {
        // Seed input.sql for table creation and data insertion.
        String sqlFilePath = "src/test/resources/data/db-connection-failing-test/input.sql";
        db.updateFromFile(sqlFilePath);
    }

    @Test
    void testCompareTwoSQLtargetsWithDefaultSettings() {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get source and target data to compare
        List<Map<String, String>> sourceRows = db.query("SELECT * FROM source;");
        List<Map<String, String>> targetRows = db.query("SELECT * FROM target;");

        // Completeness check: Assert that both source and target are of same size.
        assertEquals(sourceRows.size(), targetRows.size());

        // Correctness check: Assert that both source and target has same data.
        TableCompareExtension.captureRows(sourceRows, targetRows);
    }

    @Test
    void testCompareTwoSQLsAndIgnoreSomeFieldsFromComparison() throws java.io.IOException {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get source and target data to compare
        String expectedCSVFilePath = "src/test/resources/data/db-connection-failing-test/expected.csv";
        List<Map<String, String>> sourceRows = CsvUtils.convertCsvToListOfMap(expectedCSVFilePath);

        String outputSQLFilePath = "src/test/resources/data/db-connection-failing-test/output.sql";
        List<Map<String, String>> targetRows = db.queryFromFile(outputSQLFilePath);

        // Completeness check: Assert that both source and target are of same size.
        assertEquals(sourceRows.size(), targetRows.size());

        // Correctness check: Assert that both source and target has same data.
        TableCompareExtension.captureRows(sourceRows, targetRows, Set.of("AGE", "GENDER"), "ID");
    }

    @Test
    void testCompareTwoSQLsShowAnchorKeyButIgnoreAnchorFromComparison() throws java.io.IOException {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        String expectedCSVFilePath = "src/test/resources/data/db-connection-failing-test/expected.csv";
        List<Map<String, String>> sourceRows = CsvUtils.convertCsvToListOfMap(expectedCSVFilePath);

        String outputSQLFilePath = "src/test/resources/data/db-connection-failing-test/output.sql";
        List<Map<String, String>> targetRows = db.queryFromFile(outputSQLFilePath);

        // Completeness check: Assert that both source and target are of same size.
        assertEquals(sourceRows.size(), targetRows.size());

        // Correctness check: Assert that both source and target has same data.
        TableCompareExtension.captureRows(sourceRows, targetRows, null, "ID");
    }

    @AfterAll
    static void tearDownAll() {
        db.update("DROP TABLE source;");
        db.update("DROP TABLE target;");
    }
}
