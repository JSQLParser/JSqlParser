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

public class GreaterThanEquals extends ComparisonOperator {

    public GreaterThanEquals() {
        super(">=");
    }

    public GreaterThanEquals(String operator) {
        super(operator);
    }

    public GreaterThanEquals(Expression leftExpression, Expression rightExpression) {
        super(">=", leftExpression, rightExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public GreaterThanEquals withLeftExpression(Expression arg0) {
        return (GreaterThanEquals) super.withLeftExpression(arg0);
    }

    @Override
    public GreaterThanEquals withRightExpression(Expression arg0) {
        return (GreaterThanEquals) super.withRightExpression(arg0);
    }

    @Override
    public GreaterThanEquals withOldOracleJoinSyntax(int arg0) {
        return (GreaterThanEquals) super.withOldOracleJoinSyntax(arg0);
    }

    @Override
    public GreaterThanEquals withOraclePriorPosition(int arg0) {
        return (GreaterThanEquals) super.withOraclePriorPosition(arg0);
    }
}
