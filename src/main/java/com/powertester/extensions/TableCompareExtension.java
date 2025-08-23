package com.powertester.extensions;

import org.junit.jupiter.api.extension.*;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TableCompareExtension
        implements BeforeEachCallback, AfterTestExecutionCallback, AfterAllCallback {

    // Directory under Maven/Gradle target for reports
    private static final String REPORT_DIR = "test-reports";

    // Thread-local storage so each test can safely pass its rows to the extension
    private static final ThreadLocal<Captured> TL_CAPTURED = new ThreadLocal<>();

    // Optional: fields to ignore (you can change/add via setter if you like)
    private static final Set<String> DEFAULT_IGNORED_FIELDS = new HashSet<>(Collections.singletonList("ID"));

    public static void captureRows(List<Map<String, String>> inputRows,
            List<Map<String, String>> outputRows) {
        TL_CAPTURED.set(new Captured(inputRows, outputRows, DEFAULT_IGNORED_FIELDS));
    }

    /** Overload if you want custom ignored fields per test */
    public static void captureRows(List<Map<String, String>> inputRows,
            List<Map<String, String>> outputRows,
            Set<String> ignoredFields) {
        TL_CAPTURED.set(new Captured(inputRows, outputRows,
                ignoredFields == null ? DEFAULT_IGNORED_FIELDS : ignoredFields));
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
        ComparisonResult result = compare(captured.inputRows, captured.outputRows, captured.ignoredFields);

        // Render HTML
        String html = renderHtml(context, result);

        // Write file
        writeHtmlFile(context, html);

        // Fail the test if there are any differences
        if (result.diffs > 0) {
            throw new AssertionError(
                    "Table comparison failed: " + result.diffs + " differences found. See HTML report for details.");
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
        final List<Map<String, String>> inputRows;
        final List<Map<String, String>> outputRows;
        final Set<String> ignoredFields;

        Captured(List<Map<String, String>> in, List<Map<String, String>> out, Set<String> ignored) {
            this.inputRows = in == null ? List.of() : in;
            this.outputRows = out == null ? List.of() : out;
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
        final String id; // if present
        final List<Cell> cells;

        Row(String id, List<Cell> cells) {
            this.id = id;
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

    private static ComparisonResult compare(List<Map<String, String>> inputRows,
            List<Map<String, String>> outputRows,
            Set<String> ignoredFields) {
        int rowsCompared = Math.min(inputRows.size(), outputRows.size());

        // Determine field order from union of keys in first input row (fallback to
        // output if needed)
        Set<String> union = new LinkedHashSet<>();
        if (!inputRows.isEmpty())
            union.addAll(inputRows.get(0).keySet());
        else if (!outputRows.isEmpty())
            union.addAll(outputRows.get(0).keySet());

        // plus any extra keys found
        for (int i = 0; i < rowsCompared; i++) {
            union.addAll(inputRows.get(i).keySet());
            union.addAll(outputRows.get(i).keySet());
        }

        // remove ignored fields, but keep a separate ID (if present) for display
        String idField = "ID";
        boolean hasId = union.contains(idField);
        List<String> fields = union.stream()
                .filter(k -> !ignoredFields.contains(k))
                .sorted()
                .collect(Collectors.toList());

        int diffs = 0;
        int cellsCompared = 0;
        List<Row> rows = new ArrayList<>();

        for (int i = 0; i < rowsCompared; i++) {
            Map<String, String> in = inputRows.get(i);
            Map<String, String> out = outputRows.get(i);

            String id = hasId ? Objects.toString(in.getOrDefault(idField, out.get(idField)), "") : null;

            List<Cell> cells = new ArrayList<>();
            for (String f : fields) {
                String iv = in.get(f);
                String ov = out.get(f);
                Cell c = new Cell(iv, ov);
                cells.add(c);
                cellsCompared++;
                if (!c.equal)
                    diffs++;
            }
            rows.add(new Row(id, cells));
        }

        return new ComparisonResult(fields, rows, rowsCompared, cellsCompared, diffs);
    }

    // --- HTML rendering & output ---

    private static String safeFileName(String s) {
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static String renderHtml(ExtensionContext ctx, ComparisonResult r) {
        String displayName = ctx.getDisplayName();
        String testName = ctx.getRequiredTestMethod().getName();
        String className = ctx.getRequiredTestClass().getSimpleName();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'>");
        sb.append("<title>Table Compare Report - ").append(escape(displayName)).append("</title>");
        sb.append("<style>");
        sb.append("body{font-family:Arial,Helvetica,sans-serif;margin:20px;}");
        sb.append("h1{margin:0 0 10px 0;font-size:20px;}");
        sb.append(".meta{color:#555;margin-bottom:16px;font-size:12px;}");
        sb.append("table{border-collapse:collapse;width:100%;}");
        sb.append("th,td{border:1px solid #ddd;padding:6px;text-align:left;}");
        sb.append("th{background:#f5f5f5;position:sticky;top:0;}");
        sb.append("td.equal{background:#ffffff;}");
        sb.append("td.diff{background:#ffcccc;}");
        sb.append(".legend{margin:10px 0 16px 0;font-size:12px;}");
        sb.append(".badge{display:inline-block;padding:2px 8px;border-radius:10px;background:#eee;margin-right:6px;}");
        sb.append("</style></head><body>");

        sb.append("<h1>Table Compare Report</h1>");
        sb.append("<div class='meta'>");
        sb.append("<div><b>Class:</b> ").append(escape(className)).append("</div>");
        sb.append("<div><b>Test:</b> ").append(escape(testName)).append("</div>");
        sb.append("<div><b>Display name:</b> ").append(escape(displayName)).append("</div>");
        sb.append("<div><b>Generated:</b> ").append(escape(timestamp)).append("</div>");
        sb.append("</div>");

        sb.append("<div class='legend'>");
        sb.append("<span class='badge'>Rows: ").append(r.rowsCompared).append("</span>");
        sb.append("<span class='badge'>Cells: ").append(r.cellsCompared).append("</span>");
        sb.append("<span class='badge'>Diffs: ").append(r.diffs).append("</span>");
        sb.append("<span class='badge' style='background:#fff;border:1px solid #ddd;'>Equal</span>");
        sb.append("<span class='badge' style='background:#ffcccc;'>Different</span>");
        sb.append("</div>");

        sb.append("<table><thead><tr>");
        // Optional ID column if present
        boolean showId = r.rows.stream().anyMatch(row -> row.id != null);
        if (showId)
            sb.append("<th>ID</th>");
        for (String f : r.fields)
            sb.append("<th>").append(escape(f)).append("</th>");
        sb.append("</tr></thead><tbody>");

        for (Row row : r.rows) {
            sb.append("<tr>");
            if (showId)
                sb.append("<td>").append(escape(row.id == null ? "" : row.id)).append("</td>");
            for (Cell c : row.cells) {
                String cls = c.equal ? "equal" : "diff";
                sb.append("<td class='").append(cls).append("'>");
                // Show input -> output if different, else show value once
                if (c.equal) {
                    sb.append(escape(orEmpty(c.inputVal)));
                } else {
                    sb.append("<div><b>IN:</b> ").append(escape(orEmpty(c.inputVal))).append("</div>");
                    sb.append("<div><b>OUT:</b> ").append(escape(orEmpty(c.outputVal))).append("</div>");
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static void writeHtmlFile(ExtensionContext testContext, String html) throws IOException {
        String className = testContext.getRequiredTestClass().getSimpleName();
        String methodName = testContext.getRequiredTestMethod().getName();
        String fileName = safeFileName(methodName) + ".html";

        Path dir = Paths.get(REPORT_DIR, className);
        Files.createDirectories(dir);
        Path file = dir.resolve(fileName);
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(html);
        }

        log.info("[TableCompareExtension] Wrote: {}", file.toAbsolutePath());
    }
}