/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class IsUnknownExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private boolean isNot = false;

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public boolean isNot() {
        return isNot;
    }

    public void setNot(boolean isNot) {
        this.isNot = isNot;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return leftExpression + " IS" + (isNot ? " NOT" : "") + " UNKNOWN";
    }

    public IsUnknownExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public IsUnknownExpression withNot(boolean isNot) {
        this.setNot(isNot);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }
}
