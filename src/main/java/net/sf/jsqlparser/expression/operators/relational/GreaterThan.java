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
    public GreaterThan withLeftExpression(Expression arg0) {
        return (GreaterThan) super.withLeftExpression(arg0);
    }

    @Override()
    public GreaterThan withRightExpression(Expression arg0) {
        return (GreaterThan) super.withRightExpression(arg0);
    }

    @Override()
    public GreaterThan withOldOracleJoinSyntax(int arg0) {
        return (GreaterThan) super.withOldOracleJoinSyntax(arg0);
    }

    @Override()
    public GreaterThan withOraclePriorPosition(int arg0) {
        return (GreaterThan) super.withOraclePriorPosition(arg0);
    }
}
