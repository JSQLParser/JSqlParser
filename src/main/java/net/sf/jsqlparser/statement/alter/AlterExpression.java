/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2016 JSQLParser
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
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 *
 * @author toben & wrobstory
 */
@Data
public class AlterExpression {

    private AlterOperation operation;
    private String columnName;
    //private ColDataType dataType;

    private List<ColumnDataType> colDataTypeList;

    private List<String> pkColumns;
    private List<String> ukColumns;
    private String ukName;
    private Index index = null;
    private String constraintName;
    private boolean onDeleteRestrict;
    private boolean onDeleteSetNull;
    private boolean onDeleteCascade;
    private List<String> fkColumns;
    private String fkSourceTable;
    private List<String> fkSourceColumns;

    private List<ConstraintState> constraints;
    private List<String> parameters;

    public void addColDataType(String columnName, ColDataType colDataType) {
        addColDataType(new ColumnDataType(columnName, colDataType, null));
    }

    public void addColDataType(ColumnDataType columnDataType) {
        if (colDataTypeList == null) {
            colDataTypeList = new ArrayList<ColumnDataType>();
        }
        colDataTypeList.add(columnDataType);
    }

    public void addParameters(String... params) {
        if (parameters == null) {
            parameters = new ArrayList<String>();
        }
        parameters.addAll(Arrays.asList(params));
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();

        b.append(operation).append(" ");

        if (columnName != null) {
            b.append("COLUMN ").append(columnName);
        } else if (getColDataTypeList() != null) {
            if (colDataTypeList.size() > 1) {
                b.append("(");
            } else {
                b.append("COLUMN ");
            }
            b.append(PlainSelect.getStringList(colDataTypeList));
            if (colDataTypeList.size() > 1) {
                b.append(")");
            }
        } else if (constraintName != null) {
            b.append("CONSTRAINT ").append(constraintName);
        } else if (pkColumns != null) {
            b.append("PRIMARY KEY (").append(PlainSelect.getStringList(pkColumns)).append(')');
        } else if (ukColumns != null) {
            b.append("UNIQUE KEY ").append(ukName).append(" (").append(PlainSelect.
                    getStringList(ukColumns)).append(")");
        } else if (fkColumns != null) {
            b.append("FOREIGN KEY (").append(PlainSelect.getStringList(fkColumns)).
                    append(") REFERENCES ").append(fkSourceTable).append(" (").append(
                    PlainSelect.getStringList(fkSourceColumns)).append(")");
            if (isOnDeleteCascade()) {
                b.append(" ON DELETE CASCADE");
            } else if (isOnDeleteRestrict()) {
                b.append(" ON DELETE RESTRICT");
            } else if (isOnDeleteSetNull()) {
                b.append(" ON DELETE SET NULL");
            }
        } else if (index != null) {
            b.append(index);
        }
        if (getConstraints() != null && !getConstraints().isEmpty()) {
            b.append(' ').append(PlainSelect.getStringList(constraints, false, false));
        }
        if (parameters!=null && !parameters.isEmpty()) {
            b.append(' ').append(PlainSelect.getStringList(parameters, false, false));
        }

        return b.toString();
    }

    @Data
    public static class ColumnDataType {

        private final String columnName;
        private final ColDataType colDataType;
        private final List<String> columnSpecs;

        public ColumnDataType(String columnName, ColDataType colDataType, List<String> columnSpecs) {
            this.columnName = columnName;
            this.colDataType = colDataType;
            this.columnSpecs = columnSpecs;
        }

        public List<String> getColumnSpecs() {
            if (columnSpecs == null) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(columnSpecs);
        }

        @Override
        public String toString() {
            return columnName + " " + colDataType + parametersToString();
        }

        private String parametersToString() {
            if (columnSpecs == null || columnSpecs.isEmpty()) {
                return "";
            }
            return " " + PlainSelect.getStringList(columnSpecs, false, false);
        }
    }

}
