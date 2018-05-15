/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * A column definition in a CREATE TABLE statement.<br>
 * Example: mycol VARCHAR(30) NOT NULL
 */
public class ColumnDefinition {

    private String columnName;
    private ColDataType colDataType;
    private List<String> columnSpecStrings;

    /**
     * A list of strings of every word after the datatype of the column.<br>
     * Example ("NOT", "NULL")
     */
    public List<String> getColumnSpecStrings() {
        return columnSpecStrings;
    }

    public void setColumnSpecStrings(List<String> list) {
        columnSpecStrings = list;
    }

    /**
     * The {@link ColDataType} of this column definition
     */
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
