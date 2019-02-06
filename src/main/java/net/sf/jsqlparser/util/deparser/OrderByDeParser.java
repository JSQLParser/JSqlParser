/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.Iterator;
import java.util.List;

public class OrderByDeParser {

    protected StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;

    OrderByDeParser() {
    }

    public OrderByDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        this.expressionVisitor = expressionVisitor;
        this.buffer = buffer;
    }

    public void deParse(List<OrderByElement> orderByElementList) {
        deParse(false, orderByElementList);
    }

    public void deParse(boolean oracleSiblings, List<OrderByElement> orderByElementList) {
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

    public void deParseElement(OrderByElement orderBy) {
        orderBy.getExpression().accept(expressionVisitor);
        if (!orderBy.isAsc()) {
            buffer.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            buffer.append(" ASC");
        }
        if (orderBy.getNullOrdering() != null) {
            buffer.append(' ');
            buffer.
                    append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }
    }

    void setExpressionVisitor(ExpressionVisitor expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }
}
