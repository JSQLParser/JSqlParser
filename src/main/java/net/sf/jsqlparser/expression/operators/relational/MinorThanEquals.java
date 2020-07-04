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

    @Override()
    public MinorThanEquals leftExpression(Expression arg0) {
        return (MinorThanEquals) super.leftExpression(arg0);
    }

    @Override()
    public MinorThanEquals rightExpression(Expression arg0) {
        return (MinorThanEquals) super.rightExpression(arg0);
    }

    @Override()
    public MinorThanEquals oldOracleJoinSyntax(int arg0) {
        return (MinorThanEquals) super.oldOracleJoinSyntax(arg0);
    }

    @Override()
    public MinorThanEquals oraclePriorPosition(int arg0) {
        return (MinorThanEquals) super.oraclePriorPosition(arg0);
    }
}
