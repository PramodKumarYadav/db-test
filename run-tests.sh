#!/bin/bash
mvn clean test
if [ $? -ne 0 ]; then
  mvn allure:report
  mvn allure:serve
fi
