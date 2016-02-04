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
package net.sf.jsqlparser.statement.alter;

import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 *
 * @author toben
 */
public class Alter implements Statement {

    private Table table;
    private String columnName;
    private ColDataType dataType;
    private List<String> pkColumns;
    private List<String> ukColumns;
    private String ukName;
    private Index index = null;
	private String operation;
    private String constraintName;
    private boolean onDeleteRestrict;
    private boolean onDeleteSetNull;
    private boolean onDeleteCascade;
    private List<String> fkColumns;
    private String fkSourceTable;
    private List<String> fkSourceColumns;

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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public List<String> getFkSourceColumns() {
        return fkSourceColumns;
    }

    public void setFkSourceColumns(List<String> fkSourceColumns) {
        this.fkSourceColumns = fkSourceColumns;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
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

    public ColDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColDataType dataType) {
        this.dataType = dataType;
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

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("ALTER TABLE ").append(table.getFullyQualifiedName()).append(" ").append(operation).append(" ");
        if (columnName != null) {
            b.append("COLUMN ").append(columnName);
            if (dataType != null) {
                b.append(" ").append(dataType.toString());
            }
        } else if (constraintName != null) {
            b.append("CONSTRAINT ").append(constraintName);
        } else if (pkColumns != null) {
            b.append("PRIMARY KEY (").append(PlainSelect.getStringList(pkColumns)).append(")");
        } else if (ukColumns != null) {
            b.append("UNIQUE KEY ").append(ukName).append(" (").append(PlainSelect.getStringList(ukColumns)).append(")");
        } else if (fkColumns != null) {
            b.append("FOREIGN KEY (").append(PlainSelect.getStringList(fkColumns)).append(") REFERENCES ").append(fkSourceTable).append(" (").append(
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
		return b.toString();
    }
}
