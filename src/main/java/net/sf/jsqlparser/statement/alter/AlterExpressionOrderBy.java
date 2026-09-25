/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.OrderByElement;

/** MySQL ALTER TABLE ORDER BY, using the same mutable order elements as query ordering. */
public class AlterExpressionOrderBy extends AlterExpression {
    private final List<OrderByElement> orderByElements = new ArrayList<>();

    public AlterExpressionOrderBy() {
        setOperation(AlterOperation.ORDER_BY);
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    @Override
    protected void appendBody(StringBuilder builder) {
        appendOrderBy(builder, builder::append);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        appendOrderBy(builder, expressionPrinter);
        appendCommonTail(builder);
        return builder;
    }

    private void appendOrderBy(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append("ORDER BY ");
        for (int i = 0; i < orderByElements.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            OrderByElement element = orderByElements.get(i);
            expressionPrinter.accept(element.getExpression());
            if (!element.isAsc()) {
                builder.append(" DESC");
            } else if (element.isAscDescPresent()) {
                builder.append(" ASC");
            }
        }
    }
}
