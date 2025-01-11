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

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class OrderByDeParser extends AbstractDeParser<List<OrderByElement>> {

    private ExpressionVisitor<StringBuilder> expressionVisitor;

    OrderByDeParser() {
        super(new StringBuilder());
    }

    public OrderByDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(List<OrderByElement> orderByElementList) {
        deParse(false, orderByElementList);
    }

    public void deParse(boolean oracleSiblings, List<OrderByElement> orderByElementList) {
        if (oracleSiblings) {
            builder.append(" ORDER SIBLINGS BY ");
        } else {
            builder.append(" ORDER BY ");
        }

        for (Iterator<OrderByElement> iterator = orderByElementList.iterator(); iterator
                .hasNext();) {
            OrderByElement orderByElement = iterator.next();
            deParseElement(orderByElement);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
    }

    public void deParseElement(OrderByElement orderBy) {
        orderBy.getExpression().accept(expressionVisitor, null);
        if (!orderBy.isAsc()) {
            builder.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            builder.append(" ASC");
        }
        if (orderBy.getNullOrdering() != null) {
            builder.append(' ');
            builder.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST
                    ? "NULLS FIRST"
                    : "NULLS LAST");
        }
        if (orderBy.isMysqlWithRollup()) {
            builder.append(" WITH ROLLUP");
        }
    }

    void setExpressionVisitor(ExpressionVisitor<StringBuilder> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

}
