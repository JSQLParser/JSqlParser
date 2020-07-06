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

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override()
    public GreaterThan leftExpression(Expression arg0) {
        return (GreaterThan) super.leftExpression(arg0);
    }

    @Override()
    public GreaterThan rightExpression(Expression arg0) {
        return (GreaterThan) super.rightExpression(arg0);
    }

    @Override()
    public GreaterThan oldOracleJoinSyntax(int arg0) {
        return (GreaterThan) super.oldOracleJoinSyntax(arg0);
    }

    @Override()
    public GreaterThan oraclePriorPosition(int arg0) {
        return (GreaterThan) super.oraclePriorPosition(arg0);
    }
}
