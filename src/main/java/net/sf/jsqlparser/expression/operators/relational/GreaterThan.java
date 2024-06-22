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

public class GreaterThan extends ComparisonOperator {

    public GreaterThan() {
        super(">");
    }

    public GreaterThan(Expression leftExpression, Expression rightExpression) {
        super(">", leftExpression, rightExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public GreaterThan withLeftExpression(Expression arg0) {
        return (GreaterThan) super.withLeftExpression(arg0);
    }

    @Override
    public GreaterThan withRightExpression(Expression arg0) {
        return (GreaterThan) super.withRightExpression(arg0);
    }

    @Override
    public GreaterThan withOldOracleJoinSyntax(int arg0) {
        return (GreaterThan) super.withOldOracleJoinSyntax(arg0);
    }

    @Override
    public GreaterThan withOraclePriorPosition(int arg0) {
        return (GreaterThan) super.withOraclePriorPosition(arg0);
    }
}
