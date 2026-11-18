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
import net.sf.jsqlparser.statement.select.WithFill;

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
        deParse(oracleSiblings, orderByElementList, null);
    }

    public <S> void deParse(boolean oracleSiblings, List<OrderByElement> orderByElementList,
            S context) {
        if (oracleSiblings) {
            builder.append(" ORDER SIBLINGS BY ");
        } else {
            builder.append(" ORDER BY ");
        }

        for (Iterator<OrderByElement> iterator = orderByElementList.iterator(); iterator
                .hasNext();) {
            OrderByElement orderByElement = iterator.next();
            if (context == null) {
                // Preserve the customization point used by existing subclasses.
                deParseElement(orderByElement);
            } else {
                deParseElement(orderByElement, context);
            }
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
    }

    public void deParseElement(OrderByElement orderBy) {
        deParseElement(orderBy, null);
    }

    public <S> void deParseElement(OrderByElement orderBy, S context) {
        orderBy.getExpression().accept(expressionVisitor, context);
        if (!orderBy.isAsc()) {
            builder.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            builder.append(" ASC");
        }
        if (orderBy.getUsingOperator() != null) {
            builder.append(" USING ").append(orderBy.getUsingOperator());
        }
        if (orderBy.getNullOrdering() != null) {
            builder.append(' ');
            builder.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST
                    ? "NULLS FIRST"
                    : "NULLS LAST");
        }
        if (orderBy.getWithFill() != null) {
            builder.append(' ');
            deParseWithFill(orderBy.getWithFill(), context);
        }
        if (orderBy.isMysqlWithRollup()) {
            builder.append(" WITH ROLLUP");
        }
    }

    private <S> void deParseWithFill(WithFill withFill, S context) {
        builder.append("WITH FILL");
        if (withFill.getFrom() != null) {
            builder.append(" FROM ");
            withFill.getFrom().accept(expressionVisitor, context);
        }
        if (withFill.getTo() != null) {
            builder.append(" TO ");
            withFill.getTo().accept(expressionVisitor, context);
        }
        if (withFill.getStep() != null) {
            builder.append(" STEP ");
            withFill.getStep().accept(expressionVisitor, context);
        }
        if (withFill.getStaleness() != null) {
            builder.append(" STALENESS ");
            withFill.getStaleness().accept(expressionVisitor, context);
        }
    }

    void setExpressionVisitor(ExpressionVisitor<StringBuilder> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

}
