/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.arithmetic;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class Multiplication extends BinaryExpression {

    public Multiplication() {}

    public Multiplication(Expression leftExpression, Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String getStringExpression() {
        return "*";
    }

    @Override
    public Multiplication withLeftExpression(Expression arg0) {
        return (Multiplication) super.withLeftExpression(arg0);
    }

    @Override
    public Multiplication withRightExpression(Expression arg0) {
        return (Multiplication) super.withRightExpression(arg0);
    }
}
