package com.powertester.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class SqlUtils {

  public static String readSqlFromFile(String filePath) {
    StringBuilder sql = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sql.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sql.toString();
  }

  public static List<String> readSqlStatements(String sqlFilePath) {
      List<String> statements = new ArrayList<>();
      try (BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {
          StringBuilder queryBuilder = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
              line = line.trim();
              if (line.isEmpty() || line.startsWith("--")) continue;
              queryBuilder.append(line);
              if (line.endsWith(";")) {
                  statements.add(queryBuilder.toString().replace(";", ""));
                  queryBuilder.setLength(0);
              } else {
                  queryBuilder.append(" ");
              }
          }
      } catch (Exception e) {
          throw new SqlFileReadException("Failed to read SQL file: " + sqlFilePath, e);
      }
      return statements;
  }
}

/**
 * Custom exception for SQL file reading errors.
 */
class SqlFileReadException extends RuntimeException {
    public SqlFileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
