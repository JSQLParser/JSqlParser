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

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;

@Data
public class Pivot {

    private List<FunctionItem> functionItems;
    private List<Column> forColumns;
    private List<SelectExpressionItem> singleInItems;
    private List<ExpressionListItem> multiInItems;
    private Alias alias;

    public void accept(PivotVisitor pivotVisitor) {
        pivotVisitor.visit(this);
    }

    public List<?> getInItems() {
        return singleInItems == null ? multiInItems : singleInItems;
    }

    @Override
    public String toString() {
        return "PIVOT ("
            + PlainSelect.getStringList(functionItems)
            + " FOR " + PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1)
            + " IN " + PlainSelect.getStringList(getInItems(), true, true) + ")"
            + (alias != null ? alias.toString() : "");
    }
}
