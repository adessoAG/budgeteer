package org.wickedsource.budgeteer.importer.resourceplan;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.wickedsource.budgeteer.imports.api.*;

import java.io.IOException;
import java.util.*;

public class ResourcePlanImporter implements PlanRecordsImporter {

    private static int RESOURCE_PLAN_SHEET_INDEX = 0;

    private static int COLUMN_PERSON = 0;

    private static int COLUMN_BUDGET = 1;

    private static int COLUMN_DAILY_RATE = 2;

    private static int FIRST_ENTRY_COLUMN = 5;

    private static int FIRST_ENTRY_ROW = 1;

    private List<List<String>> skippedRecords = new LinkedList<List<String>>();

    @Override
    public List<ImportedPlanRecord> importFile(ImportFile file, CurrencyUnit currencyUnit) throws ImportException {
        LinkedList<String> filenameList= new LinkedList<String>();
        filenameList.add(file.getFilename());
        skippedRecords.add(filenameList);
        skippedRecords.add(new LinkedList<String>());

        try {
            List<ImportedPlanRecord> resultList = new ArrayList<ImportedPlanRecord>();
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(RESOURCE_PLAN_SHEET_INDEX);
            List<DateColumn> dateColumns = getDateColumns(sheet);
            int i = FIRST_ENTRY_ROW;
            Row row = sheet.getRow(i);
            while (row != null && row.getCell(0).getStringCellValue() != null) {
                List<ImportedPlanRecord> records = parseRow(row, dateColumns, currencyUnit, skippedRecords);
                resultList.addAll(records);
                row = sheet.getRow(++i);
            }
            return resultList;
        } catch (IOException e) {
            throw new ImportException(e);
        }
    }

    private List<DateColumn> getDateColumns(Sheet sheet) {
        List<DateColumn> columns = new ArrayList<DateColumn>();
        int i = FIRST_ENTRY_COLUMN;
        Cell dateCell = sheet.getRow(0).getCell(i);
        while (dateCell != null && dateCell.getDateCellValue() != null) {
            Date date = dateCell.getDateCellValue();
            DateColumn dateColumn = new DateColumn(date, i);
            columns.add(dateColumn);
            dateCell = sheet.getRow(0).getCell(++i);
        }
        return columns;
    }

    private List<ImportedPlanRecord> parseRow(Row row, List<DateColumn> dateColumns, CurrencyUnit currencyUnit, List<List<String>> skippedRecords) {
        List<ImportedPlanRecord> recordsList = new ArrayList<ImportedPlanRecord>();

        for (DateColumn dateColumn : dateColumns) {

            Cell hoursCell = row.getCell(dateColumn.getColumnIndex());
            if (hoursCell != null) {
                double hoursPlanned = row.getCell(dateColumn.getColumnIndex()).getNumericCellValue();
                if (hoursPlanned > 0) {
                    int minutesPlanned = (int) (hoursPlanned * 60);
                    ImportedPlanRecord record = new ImportedPlanRecord();
                    record.setPersonName(row.getCell(COLUMN_PERSON).getStringCellValue());
                    record.setBudgetName(row.getCell(COLUMN_BUDGET).getStringCellValue());
                    record.setDailyRate(Money.of(currencyUnit, row.getCell(COLUMN_DAILY_RATE).getNumericCellValue()));
                    record.setMinutesPlanned(minutesPlanned);
                    record.setDate(dateColumn.getDate());
                    recordsList.add(record);
                }
                skippedRecords.add(getRowAsString(row));
            }
        }

        return recordsList;
    }

    private List<String> getRowAsString(Row row) {
        List<String> result = new LinkedList<String>();
        for(short i=row.getFirstCellNum(); i<row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if(cell == null) {
                result.add("");
                continue;
            }
            result.add(cell.toString());
        }
        result.add("");
        result.add("Record without hours");
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Resource Plan Importer";
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return Arrays.asList(".xslx");
    }

    @Override
    public ExampleFile getExampleFile() {
        ExampleFile file = new ExampleFile();
        file.setFileName("resource_plan.xlsx");
        file.setInputStream(getClass().getResourceAsStream("/example_resource_plan.xlsx"));
        file.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return file;
    }

    @Override
    public List<List<String>> getSkippedDataSets() {
        return skippedRecords;
    }
}
