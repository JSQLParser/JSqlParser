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

import net.sf.jsqlparser.schema.Column;

import java.util.List;

public class Pivot {

    private List<FunctionItem> functionItems;

    private List<Column> forColumns;

    private List<SelectExpressionItem> inItems;

    public List<FunctionItem> getFunctionItems() {
        return functionItems;
    }

    public void setFunctionItems(List<FunctionItem> functionItems) {
        this.functionItems = functionItems;
    }

    public List<Column> getForColumns() {
        return forColumns;
    }

    public void setForColumns(List<Column> forColumns) {
        this.forColumns = forColumns;
    }

    public List<SelectExpressionItem> getInItems() {
        return inItems;
    }

    public void setInItems(List<SelectExpressionItem> inItems) {
        this.inItems = inItems;
    }

    @Override
    public String toString() {
        return "PIVOT (" +
                PlainSelect.getStringList(functionItems) +
                " FOR " + PlainSelect.getStringList(forColumns, true, forColumns.size() > 1) +
                " IN " + PlainSelect.getStringList(inItems, true, true) + ")";
    }
}
