/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.conditional;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class OrExpression extends BinaryExpression {

    public OrExpression() {
        // nothing
    }

    public OrExpression(Expression leftExpression, Expression rightExpression) {
        setLeftExpression(leftExpression);
        setRightExpression(rightExpression);
    }

    @Override
    public OrExpression leftExpression(Expression expression) {
        return (OrExpression) super.leftExpression(expression);
    }

    @Override
    public OrExpression rightExpression(Expression expression) {
        return (OrExpression) super.rightExpression(expression);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return "OR";
    }

    public static OrExpression create(Expression left, Expression right) {
        return new OrExpression().leftExpression(left).rightExpression(right);
    }
}
