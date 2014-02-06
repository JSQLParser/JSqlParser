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
package net.sf.jsqlparser.statement.select;

/**
 * A top clause in the form [TOP (row_count) or TOP row_count]
 */
public class Top {

    private long rowCount;
    private boolean rowCountJdbcParameter = false;
    private boolean hasParenthesis = false;

    public long getRowCount() {
        return rowCount;
    }

    // TODO instead of a plain number, an expression should be added, which could be a NumberExpression, a GroupedExpression or a JdbcParameter
    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public boolean isRowCountJdbcParameter() {
        return rowCountJdbcParameter;
    }

    public void setRowCountJdbcParameter(boolean rowCountJdbcParameter) {
        this.rowCountJdbcParameter = rowCountJdbcParameter;
    }

    public boolean hasParenthesis() {
        return hasParenthesis;
    }

    public void setParenthesis(boolean hasParenthesis) {
        this.hasParenthesis = hasParenthesis;
    }

    @Override
    public String toString() {
        String result = "TOP ";

        if (hasParenthesis) {
            result += "(";
        }

        result += rowCountJdbcParameter ? "?"
                : rowCount;

        if (hasParenthesis) {
            result += ")";
        }

        return result;
    }
}
