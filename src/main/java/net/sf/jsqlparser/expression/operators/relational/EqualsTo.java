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

public class EqualsTo extends ComparisonOperator {

    public EqualsTo() {
        super("=");
    }

    public EqualsTo(Expression left, Expression right) {
        this();
        setLeftExpression(left);
        setRightExpression(right);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public EqualsTo leftExpression(Expression expression) {
        return (EqualsTo) super.leftExpression(expression);
    }

    @Override
    public EqualsTo rightExpression(Expression expression) {
        return (EqualsTo) super.rightExpression(expression);
    }

    public static EqualsTo create(Expression left, Expression right) {
        return new EqualsTo().leftExpression(left).rightExpression(right);
    }

}
