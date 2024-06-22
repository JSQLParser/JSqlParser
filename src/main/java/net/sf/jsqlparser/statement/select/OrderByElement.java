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

import java.io.Serializable;
import net.sf.jsqlparser.expression.Expression;

public class OrderByElement implements Serializable {

    public enum NullOrdering {
        NULLS_FIRST, NULLS_LAST;

        public static NullOrdering from(String ordering) {
            return Enum.valueOf(NullOrdering.class, ordering.toUpperCase());
        }
    }

    private Expression expression;
    // postgres rollup is an ExpressionList
    private boolean mysqlWithRollup = false;
    private boolean asc = true;
    private boolean ascDescPresent = false;
    private NullOrdering nullOrdering;

    public boolean isAsc() {
        return asc;
    }

    public NullOrdering getNullOrdering() {
        return nullOrdering;
    }

    public void setNullOrdering(NullOrdering nullOrdering) {
        this.nullOrdering = nullOrdering;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public void setAscDescPresent(boolean ascDescPresent) {
        this.ascDescPresent = ascDescPresent;
    }

    public boolean isAscDescPresent() {
        return ascDescPresent;
    }

    public <T, S> T accept(OrderByVisitor<T> orderByVisitor, S arguments) {
        return orderByVisitor.visit(this, arguments);
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
        } else if (ascDescPresent) {
            b.append(" ASC");
        }

        if (nullOrdering != null) {
            b.append(' ');
            b.append(nullOrdering == NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }
        if (isMysqlWithRollup()) {
            b.append(" WITH ROLLUP");
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

    public OrderByElement withAscDescPresent(boolean ascDescPresent) {
        this.setAscDescPresent(ascDescPresent);
        return this;
    }

    public OrderByElement withNullOrdering(NullOrdering nullOrdering) {
        this.setNullOrdering(nullOrdering);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

    public boolean isMysqlWithRollup() {
        return mysqlWithRollup;
    }

    public OrderByElement setMysqlWithRollup(boolean mysqlWithRollup) {
        this.mysqlWithRollup = mysqlWithRollup;
        return this;
    }

}
