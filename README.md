# db-test

A lightweight framework to allow tests for ETL scenarios and comparing table to table sql outputs. The results are presented in a html file that gives a row by row compare between source and target

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