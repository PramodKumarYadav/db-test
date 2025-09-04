package com.powertester.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SqlFileReader {

  public static String readSqlFromFile(String filePath) {
    StringBuilder sql = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        sql.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sql.toString();
  }
}
