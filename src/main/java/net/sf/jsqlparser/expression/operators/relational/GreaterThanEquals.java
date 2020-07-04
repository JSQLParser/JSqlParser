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

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override()
    public GreaterThanEquals leftExpression(Expression arg0) {
        return (GreaterThanEquals) super.leftExpression(arg0);
    }

    @Override()
    public GreaterThanEquals rightExpression(Expression arg0) {
        return (GreaterThanEquals) super.rightExpression(arg0);
    }

    @Override()
    public GreaterThanEquals oldOracleJoinSyntax(int arg0) {
        return (GreaterThanEquals) super.oldOracleJoinSyntax(arg0);
    }

    @Override()
    public GreaterThanEquals oraclePriorPosition(int arg0) {
        return (GreaterThanEquals) super.oraclePriorPosition(arg0);
    }
}
