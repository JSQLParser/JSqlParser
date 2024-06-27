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
    public NotEqualsTo withLeftExpression(Expression expression) {
        return (NotEqualsTo) super.withLeftExpression(expression);
    }

    @Override
    public NotEqualsTo withRightExpression(Expression expression) {
        return (NotEqualsTo) super.withRightExpression(expression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public NotEqualsTo withOldOracleJoinSyntax(int arg0) {
        return (NotEqualsTo) super.withOldOracleJoinSyntax(arg0);
    }

    @Override
    public NotEqualsTo withOraclePriorPosition(int arg0) {
        return (NotEqualsTo) super.withOraclePriorPosition(arg0);
    }
}
