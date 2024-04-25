/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

/**
 * @deprecated This class is deprecated since version 5.0. Use {@link ParenthesedExpressionList}
 *             instead. The reason for deprecation is the ambiguity and redundancy.
 */
@Deprecated(since = "5.0", forRemoval = true)
public class Parenthesis extends ParenthesedExpressionList<Expression> {
    public Expression getExpression() {
        return isEmpty() ? null : get(0);
    }

    public Parenthesis setExpression(Expression expression) {
        this.set(0, expression);
        return this;
    }

    public Parenthesis withExpression(Expression expression) {
        return this.setExpression(expression);
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

}
