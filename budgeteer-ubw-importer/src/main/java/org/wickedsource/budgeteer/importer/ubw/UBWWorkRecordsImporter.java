package org.wickedsource.budgeteer.importer.ubw;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wickedsource.budgeteer.imports.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

public class UBWWorkRecordsImporter implements WorkRecordsImporter {

    private static final int CONTENT_ROW_INDEX = 3;
    private List<List<String>> skippedRecords = new ArrayList<>();

    @Override
    public List<ImportedWorkRecord> importFile(ImportFile file) throws ImportException, InvalidFileFormatException {
        try {
            skippedRecords.add(new ArrayList<>());
            //Adds the name of the imported file at the beginning of the list of skipped data sets..
            List<String> fileName = new ArrayList<>();
            fileName.add(file.getFilename());
            skippedRecords.add(fileName);

            List<ImportedWorkRecord> resultList = new ArrayList<>();
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            TableInfo tableInfo = findTableInfoForValidSheet(workbook).orElseThrow(() -> new InvalidFileFormatException("Invalid file", file.getFilename()));
            Sheet sheet = tableInfo.getSheet();

            StreamSupport.stream(sheet.spliterator(), false)
                    .skip(CONTENT_ROW_INDEX)
                    .filter(row -> row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue() != null)
                    .forEach(row -> {
                        if (isImportable(row, tableInfo)) {
                            ImportedWorkRecord record = parseRow(row, tableInfo, file);
                            resultList.add(record);
                        } else {
                            if (!isCompletelyEmpty(row)) {
                                skippedRecords.add(getRowAsStrings(row));
                            }
                        }
                    });

            return resultList;
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

    @Override
    public String getDisplayName() {
        return "UBW Working Hours Importer";
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return Collections.singletonList(".xlsx");
    }

    @Override
    public ExampleFile getExampleFile() {
        ExampleFile file = new ExampleFile();
        file.setFileName("ubw_report.xlsx");
        file.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        try {
            Date date = new Date();
            Calendar maxCalendar = Calendar.getInstance();
            Calendar maxineCalendar = Calendar.getInstance();
            XSSFWorkbook workbook = new XSSFWorkbook(getClass().getResourceAsStream("/example_ubw_report.xlsx"));
            TableInfo tableInfo = findTableInfoForValidSheet(workbook).orElseThrow(RuntimeException::new);
            Sheet sheet = tableInfo.getSheet();

            XSSFCellStyle style = workbook.createCellStyle();
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setDataFormat((short) 14);

            maxCalendar.setTime(date);
            maxineCalendar.setTime(date);

            for (int i = tableInfo.getSheet().getLastRowNum(); i != 0; i--) {
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(CONTENT_ROW_INDEX - 1);
                if (cell.getStringCellValue().equals("Mustermann, Max")) {
                    createCell(maxCalendar, row, style);
                } else if (cell.getStringCellValue().equals("Mustermann, Maxine")) {
                    createCell(maxineCalendar, row, style);
                }
            }

            File tmp = File.createTempFile("example_ubw_report", ".xlsx");
            try (FileOutputStream fileOutputStream = new FileOutputStream(tmp)) {
                workbook.write(fileOutputStream);
            }
            file.setInputStream(new FileInputStream(tmp));
        } catch (IOException e) {
            file.setInputStream(getClass().getResourceAsStream("/example_ubw_report.xlsx"));
        }
        return file;
    }

    @Override
    public List<List<String>> getSkippedRecords() {
        //if just an empty row at the beginning and the filename is in the List of skipped records, return an empty List
        if (skippedRecords != null && skippedRecords.size() == 2) {
            skippedRecords = new ArrayList<>();
        }
        return skippedRecords;
    }

    Optional<TableInfo> findTableInfoForValidSheet(Workbook workbook) {
        return StreamSupport.stream(workbook.spliterator(), false)
                .map(TableInfo::new)
                .filter(TableInfo::isValid)
                .findFirst();
    }

    private List<String> getRowAsStrings(Row row) {
        List<String> result = new ArrayList<>();
        for (short i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                result.add("");
                continue;
            }
            result.add(cell.toString());
        }
        result.add("");
        result.add("Line: " + row.getRowNum());
        result.add("Record is not importable");
        return result;
    }

    private ImportedWorkRecord parseRow(Row row, TableInfo tableInfo, ImportFile file) throws ImportException {
        try {
            String personName = row.getCell(tableInfo.getColumnPerson()).getStringCellValue();
            Date date = row.getCell(tableInfo.getColumnDate()).getDateCellValue();
            String budgetName = row.getCell(tableInfo.getColumnBudget()).getStringCellValue();
            double hours = row.getCell(tableInfo.getColumnHours()).getNumericCellValue();

            ImportedWorkRecord record = new ImportedWorkRecord();
            record.setDate(date);
            record.setBudgetName(budgetName);
            record.setPersonName(personName);
            record.setMinutesWorked((int) Math.round(hours * 60));

            if (record.getDate() == null) {
                throw new ImportException(String.format("Missing date in row %d and column %d of file %s", row.getRowNum() + 1, tableInfo.getColumnDate() + 1, file.getFilename()));
            }

            return record;
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException(e);
        }
    }

    private void createCell(Calendar calendar, Row row, CellStyle style) {
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            calendar.add(Calendar.DATE, -1);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -2);
        }

        Cell cell = row.createCell(3);
        cell.setCellValue(calendar.getTime());
        cell.setCellStyle(style);
        calendar.add(Calendar.DATE, -1);
    }

    private boolean isImportable(Row row, TableInfo tableInfo) {
        return row != null && ("ja".equalsIgnoreCase(row.getCell(tableInfo.getColumnInvoiceable()).getStringCellValue()))
                && (!isBlank(row.getCell(tableInfo.getColumnBudget()).getStringCellValue()))
                && (!isBlank(row.getCell(tableInfo.getColumnPerson()).getStringCellValue()));
    }


    private boolean isCompletelyEmpty(Row row) {
        for (short i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (!isBlank(cell.toString())) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
