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

public class MinorThan extends ComparisonOperator {

    public MinorThan() {
        super("<");
    }

    public MinorThan(Expression leftExpression, Expression rightExpression) {
        super("<", leftExpression, rightExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public MinorThan withLeftExpression(Expression arg0) {
        return (MinorThan) super.withLeftExpression(arg0);
    }

    @Override
    public MinorThan withRightExpression(Expression arg0) {
        return (MinorThan) super.withRightExpression(arg0);
    }

    @Override
    public MinorThan withOldOracleJoinSyntax(int arg0) {
        return (MinorThan) super.withOldOracleJoinSyntax(arg0);
    }

    @Override
    public MinorThan withOraclePriorPosition(int arg0) {
        return (MinorThan) super.withOraclePriorPosition(arg0);
    }
}
