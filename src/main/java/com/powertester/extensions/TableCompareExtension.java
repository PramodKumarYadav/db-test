package com.powertester.extensions;

import org.junit.jupiter.api.extension.*;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class TableCompareExtension
        implements BeforeEachCallback, AfterTestExecutionCallback, AfterAllCallback {

    // Directory under Maven/Gradle target for reports
    private static final String REPORT_DIR = "test-reports";

    // Thread-local storage so each test can safely pass its rows to the extension
    private static final ThreadLocal<Captured> TL_CAPTURED = new ThreadLocal<>();


    public static void captureRows(List<Map<String, String>> expectedRows,
            List<Map<String, String>> actualRows) {
        TL_CAPTURED.set(new Captured(expectedRows, actualRows, null));
    }

    /** Overload if you want custom ignored fields per test */
    public static void captureRows(List<Map<String, String>> expectedRows,
            List<Map<String, String>> actualRows,
            Set<String> ignoredFields) {
        TL_CAPTURED.set(new Captured(expectedRows, actualRows,ignoredFields));
    }
    
    @Override
    public void beforeEach(ExtensionContext context) {
        // Clear any previous capture just in case the same thread is reused
        TL_CAPTURED.remove();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Captured captured = TL_CAPTURED.get();
        if (captured == null) {
            // Nothing captured for this test—no report to write.
            return;
        }

        // Build comparison model
        ComparisonResult result = compare(captured.expectedRows, captured.actualRows, captured.ignoredFields);

        // Render HTML
        String html = renderHtml(context, result);

        // Write file and get path
        Path reportPath = writeHtmlFile(context, html);

        // Add these detailed compare reports as an attachment step to each test in allure report.
        String reportLink = reportPath.toAbsolutePath().toString();
        File report = new File(reportLink);
        if (report.isFile()) {
            Allure.addAttachment("Table Compare Report", new FileInputStream(report));
        } else {
            Allure.step("No table-compare HTML found at: " + report.getAbsolutePath());
        }

        // Fail the test if there are any differences
        if (result.diffs > 0) {      
            throw new AssertionError(
                "Table comparison failed: " + result.diffs + " differences found. "
                            + "See HTML report: " + reportLink);
        }

        // Cleanup
        TL_CAPTURED.remove();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Could aggregate or add an index here if desired.
    }

    // --- Data classes ---

    private static class Captured {
        final List<Map<String, String>> expectedRows;
        final List<Map<String, String>> actualRows;
        final Set<String> ignoredFields;

        Captured(List<Map<String, String>> in, List<Map<String, String>> out, Set<String> ignored) {
            this.expectedRows = in == null ? List.of() : in;
            this.actualRows = out == null ? List.of() : out;
            this.ignoredFields = ignored == null ? Set.of() : ignored;
        }
    }

    private static class Cell {
        final String inputVal;
        final String outputVal;
        final boolean equal;

        Cell(String in, String out) {
            this.inputVal = in;
            this.outputVal = out;
            this.equal = Objects.equals(in, out);
        }
    }

    private static class Row {
        final List<Cell> cells;

        Row(List<Cell> cells) {
            this.cells = cells;
        }
    }

    private static class ComparisonResult {
        final List<String> fields; // order used in the table
        final List<Row> rows;
        final int rowsCompared;
        final int cellsCompared;
        final int diffs;

        ComparisonResult(List<String> fields, List<Row> rows, int rowsCompared, int cellsCompared, int diffs) {
            this.fields = fields;
            this.rows = rows;
            this.rowsCompared = rowsCompared;
            this.cellsCompared = cellsCompared;
            this.diffs = diffs;
        }
    }

    // --- Comparison logic ---

    private static ComparisonResult compare(List<Map<String, String>> expectedRows,
            List<Map<String, String>> actualRows,
            Set<String> ignoredFields) {

        // Compare up to the smaller of the two lists to allow for
        // comparison if there are differences in row sizes in input and output.
        // (in the end, also assert that both sizes are equal so that we test for completeness)
        int rowsCompared = Math.min(expectedRows.size(), actualRows.size());

        // Determine field order from union of keys in first input row (fallback to
        // output if needed)
        Set<String> union = new LinkedHashSet<>();
        for (int i = 0; i < rowsCompared; i++) {
            union.addAll(expectedRows.get(i).keySet());
            union.addAll(actualRows.get(i).keySet());
        }

        // remove ignored fields
        List<String> fields = union.stream()
                .filter(k -> !ignoredFields.contains(k))
                .toList();

        int diffs = 0;
        int cellsCompared = 0;
        List<Row> rows = new ArrayList<>();

        for (int i = 0; i < rowsCompared; i++) {
            Map<String, String> inRow = expectedRows.get(i);
            Map<String, String> outRow = actualRows.get(i);

            List<Cell> cells = new ArrayList<>();
            for (String fieldName : fields) {
                String inVal = inRow.get(fieldName);
                String outVal = outRow.get(fieldName);
                Cell cell = new Cell(inVal, outVal);
                cells.add(cell);
                cellsCompared++;
                if (!cell.equal) diffs++;
            }
            rows.add(new Row(cells));
        }

        return new ComparisonResult(fields, rows, rowsCompared, cellsCompared, diffs);
    }

    // --- HTML rendering & output ---
    private static String renderHtml(ExtensionContext testContext, ComparisonResult comparisonResult) {
        String displayName = testContext.getDisplayName();
        String testName = testContext.getRequiredTestMethod().getName();
        String className = testContext.getRequiredTestClass().getSimpleName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!doctype html><html><head><meta charset='utf-8'>");
        stringBuilder.append("<title>Table Compare Report - ").append(escape(displayName)).append("</title>");
        stringBuilder.append(renderStyle());
        stringBuilder.append("</head><body>");

        stringBuilder.append("<h1>Table Compare Report</h1>");
        stringBuilder.append(renderMetaInfo(className, testName, displayName, timestamp));
        stringBuilder.append(renderLegend(comparisonResult.rowsCompared, comparisonResult.cellsCompared, comparisonResult.diffs));

        stringBuilder.append("<table>");
        stringBuilder.append(renderTableHeader(comparisonResult.fields));
        stringBuilder.append(renderTableBody(comparisonResult.rows));
        stringBuilder.append("</table>");
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }

    private static String renderStyle() {
        return "<style>" +
                "body{font-family:Arial,Helvetica,sans-serif;margin:20px;}" +
                "h1{margin:0 0 10px 0;font-size:20px;}" +
                ".meta{color:#555;margin-bottom:16px;font-size:12px;}" +
                "table{border-collapse:collapse;width:100%;}" +
                "th,td{border:1px solid #ddd;padding:6px;text-align:left;}" +
                "th{background:#f5f5f5;position:sticky;top:0;}" +
                "td.equal{background:#ffffff;}" +
                "td.diff{background:#ffcccc;}" +
                ".legend{margin:10px 0 16px 0;font-size:12px;}" +
                ".badge{display:inline-block;padding:2px 8px;border-radius:10px;background:#eee;margin-right:6px;}" +
                "</style>";
    }

    private static String renderMetaInfo(String className, String testName, String displayName, String timestamp) {
        return "<div class='meta'>" +
                "<div><b>Class:</b> " + escape(className) + "</div>" +
                "<div><b>Test:</b> " + escape(testName) + "</div>" +
                "<div><b>Display name:</b> " + escape(displayName) + "</div>" +
                "<div><b>Generated:</b> " + escape(timestamp) + "</div>" +
                "</div>";
    }

    private static String renderLegend(int rowsCompared, int cellsCompared, int diffs) {
        return "<div class='legend'>" +
                "<span class='badge'>Rows: " + rowsCompared + "</span>" +
                "<span class='badge'>Cells: " + cellsCompared + "</span>" +
                "<span class='badge'>Diffs: " + diffs + "</span>" +
                "<span class='badge' style='background:#fff;border:1px solid #ddd;'>Equal</span>" +
                "<span class='badge' style='background:#ffcccc;'>Different</span>" +
                "</div>";
    }

    private static String renderTableHeader(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("<thead><tr>");
        for (String fieldName : fields) {
            sb.append("<th>").append(escape(fieldName)).append("</th>");
        }
        sb.append("</tr></thead>");
        return sb.toString();
    }

    private static String renderTableBody(List<Row> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tbody>");
        for (Row row : rows) {
            sb.append("<tr>");
            for (Cell resultCell : row.cells) {
                String cls = resultCell.equal ? "equal" : "diff";
                sb.append("<td class='").append(cls).append("'>");
                if (resultCell.equal) {
                    sb.append(escape(orEmpty(resultCell.inputVal)));
                } else {
                    sb.append("<div><b>IN:</b> ").append(escape(orEmpty(resultCell.inputVal))).append("</div>");
                    sb.append("<div><b>OUT:</b> ").append(escape(orEmpty(resultCell.outputVal))).append("</div>");
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
        return sb.toString();
    }

    private static String orEmpty(String rawInput) {
        return rawInput == null ? "" : rawInput;
    }

    private static String escape(String rawInput) {
        if (rawInput == null)
            return "";
        return rawInput.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static Path writeHtmlFile(ExtensionContext testContext, String html) throws IOException {
        String className = testContext.getRequiredTestClass().getSimpleName();
        String methodName = testContext.getRequiredTestMethod().getName();
        String fileName = safeFileName(methodName) + ".html";

        Path reportDirectory = Paths.get(REPORT_DIR, className);
        Files.createDirectories(reportDirectory);
        Path file = reportDirectory.resolve(fileName);
        try (Writer htmlWriter = new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            htmlWriter.write(html);
        }

        log.info("Test result file is here: {}", file.toAbsolutePath());
        return file;
    }

    private static String safeFileName(String rawFileName) {
        return rawFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}