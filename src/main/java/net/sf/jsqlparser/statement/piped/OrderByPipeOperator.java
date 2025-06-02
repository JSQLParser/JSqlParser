/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.List;

public class OrderByPipeOperator extends PipeOperator {
    private List<OrderByElement> orderByElements;

    public OrderByPipeOperator(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public OrderByPipeOperator setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ")
                .append("ORDER BY ");
        for (OrderByElement orderByElement : orderByElements) {
            builder.append(orderByElement);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }
}
