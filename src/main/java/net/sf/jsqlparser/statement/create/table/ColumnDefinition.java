/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Globally used definition class for columns.
 */
public class ColumnDefinition {

    private String columnName;
    private ColDataType colDataType;
    private List<String> columnSpecs;

    public ColumnDefinition() {
    }

    public ColumnDefinition(String columnName, ColDataType colDataType, List<String> columnSpecs) {
        this.columnName = columnName;
        this.colDataType = colDataType;
        this.columnSpecs = columnSpecs;
    }

    public List<String> getColumnSpecs() {
        return columnSpecs;
    }

    public void setColumnSpecs(List<String> list) {
        columnSpecs = list;
    }

    public ColDataType getColDataType() {
        return colDataType;
    }

    public void setColDataType(ColDataType type) {
        colDataType = type;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String string) {
        columnName = string;
    }

    @Override
    public String toString() {
        return columnName + " " + toStringDataTypeAndSpec();
    }

    public String toStringDataTypeAndSpec() {
        return colDataType + ((columnSpecs != null && !columnSpecs.isEmpty())
                ? " " + PlainSelect.getStringList(columnSpecs, false, false)
                : "");
    }
}
