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
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public EqualsTo withLeftExpression(Expression expression) {
        return (EqualsTo) super.withLeftExpression(expression);
    }

    @Override
    public EqualsTo withRightExpression(Expression expression) {
        return (EqualsTo) super.withRightExpression(expression);
    }

    @Override
    public EqualsTo withOldOracleJoinSyntax(int arg0) {
        return (EqualsTo) super.withOldOracleJoinSyntax(arg0);
    }

    @Override
    public EqualsTo withOraclePriorPosition(int arg0) {
        return (EqualsTo) super.withOraclePriorPosition(arg0);
    }
}
