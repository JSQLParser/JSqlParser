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
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 *
 * @author toben & wrobstory
 */
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

    public AlterOperation getOperation() {
        return operation;
    }

    public void setOperation(AlterOperation operation) {
        this.operation = operation;
    }

    public boolean isOnDeleteCascade() {
        return onDeleteCascade;
    }

    public void setOnDeleteCascade(boolean onDeleteCascade) {
        this.onDeleteCascade = onDeleteCascade;
    }

    public boolean isOnDeleteRestrict() {
        return onDeleteRestrict;
    }

    public void setOnDeleteRestrict(boolean onDeleteRestrict) {
        this.onDeleteRestrict = onDeleteRestrict;
    }

    public boolean isOnDeleteSetNull() {
        return onDeleteSetNull;
    }

    public void setOnDeleteSetNull(boolean onDeleteSetNull) {
        this.onDeleteSetNull = onDeleteSetNull;
    }

    public List<String> getFkColumns() {
        return fkColumns;
    }

    public void setFkColumns(List<String> fkColumns) {
        this.fkColumns = fkColumns;
    }

    public String getFkSourceTable() {
        return fkSourceTable;
    }

    public void setFkSourceTable(String fkSourceTable) {
        this.fkSourceTable = fkSourceTable;
    }

    public List<ColumnDataType> getColDataTypeList() {
        return colDataTypeList;
    }

    public void addColDataType(String columnName, ColDataType colDataType) {
        addColDataType(new ColumnDataType(columnName, colDataType, null));
    }

    public void addColDataType(ColumnDataType columnDataType) {
        if (colDataTypeList == null) {
            colDataTypeList = new ArrayList<ColumnDataType>();
        }
        colDataTypeList.add(columnDataType);
    }

    public List<String> getFkSourceColumns() {
        return fkSourceColumns;
    }

    public void setFkSourceColumns(List<String> fkSourceColumns) {
        this.fkSourceColumns = fkSourceColumns;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getConstraintName() {
        return this.constraintName;
    }

    public void setConstraintName(final String constraintName) {
        this.constraintName = constraintName;
    }

    public List<String> getPkColumns() {
        return pkColumns;
    }

    public void setPkColumns(List<String> pkColumns) {
        this.pkColumns = pkColumns;
    }

    public List<String> getUkColumns() {
        return ukColumns;
    }

    public void setUkColumns(List<String> ukColumns) {
        this.ukColumns = ukColumns;
    }

    public String getUkName() {
        return ukName;
    }

    public void setUkName(String ukName) {
        this.ukName = ukName;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public List<ConstraintState> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintState> constraints) {
        this.constraints = constraints;
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

        return b.toString();
    }

    public static class ColumnDataType {

        private final String columnName;
        private final ColDataType colDataType;
        private final List<String> columnSpecs;

        public ColumnDataType(String columnName, ColDataType colDataType, List<String> columnSpecs) {
            this.columnName = columnName;
            this.colDataType = colDataType;
            this.columnSpecs = columnSpecs;
        }

        public String getColumnName() {
            return columnName;
        }

        public ColDataType getColDataType() {
            return colDataType;
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
