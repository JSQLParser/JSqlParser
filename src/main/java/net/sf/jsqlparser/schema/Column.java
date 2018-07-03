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
package net.sf.jsqlparser.schema;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A column. It can have the table name it belongs to.
 */
public final class Column extends ASTNodeAccessImpl implements Expression, MultiPartName {

    private Table table;
    private String columnName;

    private String collate = null;

    public Column() {}

    public Column(Table table, String columnName) {
        setTable(table);
        setColumnName(columnName);
    }

    public Column(List<String> nameParts) {
        this(nameParts.size() > 1
            ? new Table(nameParts.subList(0, nameParts.size() - 1)) : null,
            nameParts.get(nameParts.size() - 1));
    }

    public Column(String columnName) {
        this(null, columnName);
    }

    public Column(Column column) {
        this(column.table, column.columnName);
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

    public void setColumnName(String string) {
        columnName = string;
    }

    @Override
    public String getFullyQualifiedName() {
        return getFullyQualifiedName(false);
    }

    /**
     * Get FQN.
     * @param aliases - use aliases.
     * @return
     */
    public String getFullyQualifiedName(boolean aliases) {
        StringBuilder fqn = new StringBuilder();

        if (table != null) {
            if (aliases && table.getAlias() != null) {
                fqn.append(table.getAlias().getName());
            } else {
                fqn.append(table.getFullyQualifiedName());
            }
            if (fqn.length() > 0) {
                fqn.append('.');
            }
        }
        if (columnName != null) {
            fqn.append(getName());
        }
        return fqn.toString();
    }

    /**
     * Get name.
     * @return 
     */
    public String getName() {
        StringBuilder name = new StringBuilder();

        if (columnName != null) {
            name.append(columnName);
        }

        if (collate != null) {
            name.append(" COLLATE ").append(collate);
        }

        return name.toString();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return getFullyQualifiedName(true);
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result + ((table == null) ? 0 : table.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Column other = (Column) obj;
        if (columnName == null) {
            if (other.columnName != null) {
                return false;
            }
        } else if (!columnName.equals(other.columnName)) {
            return false;
        }
        if (table == null) {
            if (other.table != null) {
                return false;
            }
        } else if (!table.equals(other.table)) {
            return false;
        }
        return true;
    }
}
