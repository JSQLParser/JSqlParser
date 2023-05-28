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
import java.io.Serializable;

public class Offset implements Serializable {

    private Expression offsetExpression = null;

    private String offsetParam = null;

    public Expression getOffset() {
        return offsetExpression;
    }

    public String getOffsetParam() {
        return offsetParam;
    }

    public void setOffset(Expression offsetExpression) {
        this.offsetExpression = offsetExpression;
    }

    public void setOffsetParam(String s) {
        offsetParam = s;
    }

    @Override
    public String toString() {
        return " OFFSET " + offsetExpression + (offsetParam != null ? " " + offsetParam : "");
    }

    public Offset withOffset(Expression offsetExpression) {
        this.setOffset(offsetExpression);
        return this;
    }

    public Offset withOffsetParam(String offsetParam) {
        this.setOffsetParam(offsetParam);
        return this;
    }

    public <E extends Expression> E getOffset(Class<E> type) {
        return type.cast(getOffset());
    }
}
