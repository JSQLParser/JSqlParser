/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;

public class OrderByElement {

    public enum NullOrdering {

        NULLS_FIRST,
        NULLS_LAST
    }

    private Expression expression;
    private boolean asc = true;
    private NullOrdering nullOrdering;
    private boolean ascDesc = false;

    public boolean isAsc() {
        return asc;
    }

    public NullOrdering getNullOrdering() {
        return nullOrdering;
    }

    public void setNullOrdering(NullOrdering nullOrdering) {
        this.nullOrdering = nullOrdering;
    }

    public void setAsc(boolean b) {
        asc = b;
    }

    public void setAscDescPresent(boolean b) {
        ascDesc = b;
    }

    public boolean isAscDescPresent() {
        return ascDesc;
    }

    public void accept(OrderByVisitor orderByVisitor) {
        orderByVisitor.visit(this);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(expression.toString());

        if (!asc) {
            b.append(" DESC");
        } else if (ascDesc) {
            b.append(" ASC");
        }

        if (nullOrdering != null) {
            b.append(' ');
            b.append(nullOrdering == NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }
        return b.toString();
    }

    public OrderByElement withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public OrderByElement withAsc(boolean asc) {
        this.setAsc(asc);
        return this;
    }

    public OrderByElement withNullOrdering(NullOrdering nullOrdering) {
        this.setNullOrdering(nullOrdering);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

}
