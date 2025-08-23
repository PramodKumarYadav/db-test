# db-test

db-test is a lightweight framework that allows to do a functional test compare of sql statement outputs. The test results for each test are then presented in a html file that gives a csv style, row by row compare between source and target records.

## Why db-test?

Comparing results of two SQL statements is easy using open source solutions such as JDBC. Asserting them with standard test libraries such as Junit5 or TestNg is also possible. However, if we use traditional test reports such as surefire or allure for these kind of tests, the output result from such assertions is hardly intutive or useful.

In context of sql output comparisons, a csv representation that highlights the differences for each row/cell feels intutive.

db-test framework does exactly that.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven

### Setup

Clone the repository:

```sh
git clone https://github.com/PramodKumarYadav/db-test.git
cd db-test
```

### Run Tests

To run all tests:

```sh
mvn clean test
```

### Test Results

Test results will be available in the `test-reports` directory as htmls. Each tests report will look like below.
![test-report](./images/test%20report.jpg)