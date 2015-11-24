/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
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
package net.sf.jsqlparser.util.deparser;


import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.Iterator;
import java.util.List;

public class OrderByDeParser {

    private StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;

    public OrderByDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        this.expressionVisitor= expressionVisitor;
        this.buffer = buffer;
    }

    public void deParse( List<OrderByElement> orderByElementList) {
        deParse(false, orderByElementList);
    }


    public void deParse(boolean oracleSiblings, List<OrderByElement> orderByElementList){
        if (oracleSiblings) {
            buffer.append(" ORDER SIBLINGS BY ");
        } else {
            buffer.append(" ORDER BY ");
        }

        for (Iterator<OrderByElement> iter = orderByElementList.iterator(); iter.hasNext();) {
            OrderByElement orderByElement = iter.next();
            deParseElement(orderByElement);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    public void deParseElement(OrderByElement orderBy){
        orderBy.getExpression().accept(expressionVisitor);
        if (!orderBy.isAsc()) {
            buffer.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            buffer.append(" ASC");
        }
        if (orderBy.getNullOrdering() != null) {
            buffer.append(' ');
            buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }
    }
}
