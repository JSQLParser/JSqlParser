/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class NotEqualsTo extends ComparisonOperator {

    public NotEqualsTo() {
        super("<>");
    }

    public NotEqualsTo(String operator) {
        super(operator);
    }

    public NotEqualsTo(Expression left, Expression right) {
        this();
        setLeftExpression(left);
        setRightExpression(right);
    }

    @Override
    public NotEqualsTo leftExpression(Expression expression) {
        return (NotEqualsTo) super.leftExpression(expression);
    }

    @Override
    public NotEqualsTo rightExpression(Expression expression) {
        return (NotEqualsTo) super.rightExpression(expression);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

}
