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

public class MinorThanEquals extends ComparisonOperator {

    public MinorThanEquals() {
        super("<=");
    }

    public MinorThanEquals(String operator) {
        super(operator);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public MinorThanEquals withLeftExpression(Expression arg0) {
        return (MinorThanEquals) super.withLeftExpression(arg0);
    }

    @Override
    public MinorThanEquals withRightExpression(Expression arg0) {
        return (MinorThanEquals) super.withRightExpression(arg0);
    }

    @Override
    public MinorThanEquals withOldOracleJoinSyntax(int arg0) {
        return (MinorThanEquals) super.withOldOracleJoinSyntax(arg0);
    }

    @Override
    public MinorThanEquals withOraclePriorPosition(int arg0) {
        return (MinorThanEquals) super.withOraclePriorPosition(arg0);
    }
}
