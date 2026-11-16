package com.corpequip.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Excel (.xlsx) read/write helpers built on Apache POI. */
public final class ExcelUtils {

    private ExcelUtils() { }

    /** Reads a sheet into a list of rows; each row maps header name to cell text. Row 1 must be the header. */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        try (FileInputStream in = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName + " in " + filePath);
            }
            DataFormatter formatter = new DataFormatter();
            Row header = sheet.getRow(0);
            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> record = new LinkedHashMap<>();
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    record.put(formatter.formatCellValue(header.getCell(c)).trim(),
                               formatter.formatCellValue(row.getCell(c)));
                }
                rows.add(record);
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + filePath, e);
        }
    }

    /** Converts rows to a TestNG DataProvider array: one Map argument per test invocation. */
    public static Object[][] toDataProvider(List<Map<String, String>> rows) {
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    /** Writes (or replaces) a sheet in a workbook, creating the file and folders when needed. */
    public static synchronized void writeSheet(String filePath, String sheetName,
                                               List<String> headers, List<List<Object>> rows) {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            File file = path.toFile();
            try (Workbook workbook = file.exists() ? new XSSFWorkbook(new FileInputStream(file)) : new XSSFWorkbook()) {
                int existing = workbook.getSheetIndex(sheetName);
                if (existing >= 0) {
                    workbook.removeSheetAt(existing);
                }
                Sheet sheet = workbook.createSheet(sheetName);
                Row headerRow = sheet.createRow(0);
                for (int c = 0; c < headers.size(); c++) {
                    headerRow.createCell(c).setCellValue(headers.get(c));
                }
                for (int r = 0; r < rows.size(); r++) {
                    Row row = sheet.createRow(r + 1);
                    for (int c = 0; c < rows.get(r).size(); c++) {
                        Cell cell = row.createCell(c);
                        cell.setCellValue(String.valueOf(rows.get(r).get(c)));
                    }
                }
                for (int c = 0; c < headers.size(); c++) {
                    sheet.setColumnWidth(c, 24 * 256);
                }
                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + filePath, e);
        }
    }
}