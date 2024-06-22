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

public class BitwiseAnd extends BinaryExpression {

    public BitwiseAnd() {}

    public BitwiseAnd(Expression leftExpression, Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public String getStringExpression() {
        return "&";
    }

    @Override
    public BitwiseAnd withLeftExpression(Expression arg0) {
        return (BitwiseAnd) super.withLeftExpression(arg0);
    }

    @Override
    public BitwiseAnd withRightExpression(Expression arg0) {
        return (BitwiseAnd) super.withRightExpression(arg0);
    }
}
