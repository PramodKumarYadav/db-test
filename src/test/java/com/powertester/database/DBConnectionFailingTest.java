package com.powertester.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import com.powertester.extensions.TableCompareExtension;

@Slf4j
@ExtendWith(TableCompareExtension.class)
class DBConnectionFailingTest {
    private static final DBConnection db = DBConnection.getInstance();

    @BeforeAll
    static void createTables() {
        // Create tables with more fields
        db.executeUpdate(
                "CREATE TABLE input (emp_id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10))");
        db.executeUpdate(
                "CREATE TABLE output (emp_id INT PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255), age INT, gender VARCHAR(10))");

        // Clean tables before each test to avoid PK violation in repeated tests
        db.executeUpdate("DELETE FROM input");
        db.executeUpdate("DELETE FROM output");
        String[] firstNames = { "John", "Jane", "Alex", "Emily", "Chris", "Pat", "Sam", "Taylor", "Jordan", "Morgan",
                "Casey", "Jamie", "Robin", "Drew", "Blake", "Cameron", "Avery", "Riley", "Quinn", "Skyler" };
        String[] lastNames = { "Doe", "Smith", "Brown", "Johnson", "Lee", "Clark", "Lewis", "Walker", "Young", "King",
                "Scott", "Green", "Baker", "Adams", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner" };
        String[] genders = { "Male", "Female" };
        java.util.Random rand = new java.util.Random();
        Set<Integer> diffRows = new HashSet<>();

        while (diffRows.size() < 14) {
            diffRows.add(1 + rand.nextInt(20));
        }

        for (int i = 1; i <= 20; i++) {
            // Set random values for rows in input table
            String randomFirstName = firstNames[rand.nextInt(firstNames.length)];
            String randomLastName = lastNames[rand.nextInt(lastNames.length)];
            int age = 20 + rand.nextInt(30);
            String gender = genders[rand.nextInt(genders.length)];

            // Set output row values to be same as input for comparison
            String outputFirstName = randomFirstName;
            String outputLastName = randomLastName;
            int outputAge = age;
            String outputGender = gender;

            // Unless we are in the diffRows set, where we want to introduce differences.
            // In that case, we will modify the output values.
            if (diffRows.contains(i)) {
                int diffType = rand.nextInt(4);
                switch (diffType) {
                    case 0:
                        outputLastName = lastNames[rand.nextInt(lastNames.length)];
                        break;
                    case 1:
                        outputAge = age + rand.nextInt(3) - 1;
                        break;
                    case 2:
                        outputGender = genders[rand.nextInt(genders.length)];
                        break;
                    case 3:
                        outputFirstName = firstNames[rand.nextInt(firstNames.length)];
                        break;
                }
            }

            db.executeUpdate(String.format(
                    "INSERT INTO input (emp_id, first_name, last_name, age, gender) VALUES (%d, '%s', '%s', %d, '%s')", i,
                    randomFirstName, randomLastName, age, gender));
            db.executeUpdate(String.format(
                    "INSERT INTO output (emp_id, first_name, last_name, age, gender) VALUES (%d, '%s', '%s', %d, '%s')", i,
                    outputFirstName, outputLastName, outputAge, outputGender));
        }
    }

    
    @Test
    void testCompareTwoSQLOutputsWithDefaultSettings() {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        List<Map<String, String>> inputRows = db.executePreparedStatement("SELECT * FROM input");
        List<Map<String, String>> outputRows = db.executePreparedStatement("SELECT * FROM output");

        // Hand the rows to the extension so it can generate the HTML after the test finishes
        TableCompareExtension.captureRows(inputRows, outputRows);
    }

    @Test
    void testCompareTwoSQLsAndIgnoreSomeFieldsFromComparison() {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        List<Map<String, String>> inputRows = db.executePreparedStatement("SELECT * FROM input");
        List<Map<String, String>> outputRows = db.executePreparedStatement("SELECT * FROM output");

        // Hand the rows to the extension so it can generate the HTML after the test finishes
        // Ignore age and gender from comparison
        TableCompareExtension.captureRows(inputRows, outputRows, Set.of("AGE", "GENDER"));
    }

    @Test
    void testCompareTwoSQLsShowAnchorKeyButIgnoreAnchorFromComparison() {
        // Arrange: input (could be done at a test, class or at project level)

        // Act: (run the application to process input data). If the app is real time like APIs, this can be done at the test level. 
        // But if the app works as a batch and takes significant time to process data, it might also make sense to do this at the project level.

        // Assert: Get input and output data to compare
        List<Map<String, String>> inputRows = db.executePreparedStatement("SELECT * FROM input");
        List<Map<String, String>> outputRows = db.executePreparedStatement("SELECT * FROM output");

        // Hand the rows to the extension so it can generate the HTML after the test finishes
        TableCompareExtension.captureRows(inputRows, outputRows, null, "EMP_ID");
    }

    @AfterAll
    static void tearDownAll() {
        db.executeUpdate("DROP TABLE input");
        db.executeUpdate("DROP TABLE output");
        db.closeConnectionPool();
    }
}
