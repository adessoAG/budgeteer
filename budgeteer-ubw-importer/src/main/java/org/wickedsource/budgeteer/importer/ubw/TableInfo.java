package org.wickedsource.budgeteer.importer.ubw;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Arrays;

public class TableInfo {

    private static final int HEADER_ROW_INDEX = 2;

    private final Sheet sheet;
    private int columnInvoiceable = -1;
    private int columnDate = -1;
    private int columnPerson = -1;
    private int columnBudget = -1;
    private int columnHours = -1;

    public TableInfo(Sheet sheet) {
        this.sheet = sheet;
        setIndices();
    }

    public boolean isValid() {
        return columnBudget >= 0 && columnDate >= 0 && columnHours >= 0 && columnInvoiceable >= 0 && columnPerson >= 0;
    }

    public Sheet getSheet() {
        return sheet;
    }

    private void setIndices() {
        Arrays.stream(UBWColumnName.values()).forEach(ubwColumnName -> {
            Row row = sheet.getRow(HEADER_ROW_INDEX);
            if (row == null) {
                return;
            }
            for (Cell cell : row) {
                if (cell.getCellType() != CellType.STRING) {
                    continue;
                }
                if (ubwColumnName.columnName.equalsIgnoreCase(cell.getStringCellValue())) {
                    switch (ubwColumnName) {
                        case EMPLOYEE_NAME:
                            this.columnPerson = cell.getColumnIndex();
                            break;
                        case DATE:
                            this.columnDate = cell.getColumnIndex();
                            break;
                        case SUB_GROUP:
                            this.columnBudget = cell.getColumnIndex();
                            break;
                        case EFFORT:
                            this.columnHours = cell.getColumnIndex();
                            break;
                        case BILLABLE:
                            this.columnInvoiceable = cell.getColumnIndex();
                            break;
                    }
                }
            }
        });
    }

    public int getColumnInvoiceable() {
        return columnInvoiceable;
    }

    public int getColumnDate() {
        return columnDate;
    }

    public int getColumnPerson() {
        return columnPerson;
    }

    public int getColumnBudget() {
        return columnBudget;
    }

    public int getColumnHours() {
        return columnHours;
    }
}
