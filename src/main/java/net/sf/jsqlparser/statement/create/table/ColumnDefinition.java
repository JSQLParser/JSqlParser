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

public class ColumnDefinition {

    private String columnName;
    private ColDataType colDataType;
    private List<String> columnSpecStrings;

    public List<String> getColumnSpecStrings() {
        return columnSpecStrings;
    }

    public void setColumnSpecStrings(List<String> list) {
        columnSpecStrings = list;
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
        return columnName + " " + colDataType + (columnSpecStrings != null ? " " + PlainSelect.
                getStringList(columnSpecStrings, false, false) : "");
    }
}
