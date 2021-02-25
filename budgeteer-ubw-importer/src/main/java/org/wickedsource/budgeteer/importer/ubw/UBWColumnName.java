package org.wickedsource.budgeteer.importer.ubw;

public enum UBWColumnName {

    EMPLOYEE_NAME("Name"),
    DATE("Tag"),
    SUB_GROUP("Subgruppe"),
    EFFORT("Aufwand [h]"),
    BILLABLE("KV");

    public final String columnName;

    UBWColumnName(String columnName){
        this.columnName = columnName;
    }
}
