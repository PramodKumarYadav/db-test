package com.powertester.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class CsvUtils {

    // Read CSV into List<Map<String, String>>
    public static List<Map<String, String>> readCsvToMapList(String filePath) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader()
                .setSkipHeaderRecord(false)
                .get();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = CSVParser.parse(reader, csvFormat)) {
            for (CSVRecord csvRow : csvParser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : csvParser.getHeaderMap().keySet()) {
                    row.put(header, csvRow.get(header));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    // Write List<Map<String, String>> to CSV
    public static void writeMapListToCsv(String filePath, List<Map<String, String>> data) throws IOException {
        if (data.isEmpty()) return;

        CSVFormat csvFormat = CSVFormat.Builder.create()
        .setHeader(data.get(0).keySet().toArray(new String[0]))
        .setSkipHeaderRecord(false)
        .get();
    
        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            for (Map<String, String> row : data) {
                csvPrinter.printRecord(row.values());
            }
        }
    }
}
