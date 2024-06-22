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

public class BitwiseOr extends BinaryExpression {

    public BitwiseOr() {}

    public BitwiseOr(Expression leftExpression, Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public String getStringExpression() {
        return "|";
    }

    @Override
    public BitwiseOr withLeftExpression(Expression arg0) {
        return (BitwiseOr) super.withLeftExpression(arg0);
    }

    @Override
    public BitwiseOr withRightExpression(Expression arg0) {
        return (BitwiseOr) super.withRightExpression(arg0);
    }
}
