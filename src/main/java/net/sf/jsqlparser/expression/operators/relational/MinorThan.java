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

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override()
    public MinorThan leftExpression(Expression arg0) {
        return (MinorThan) super.leftExpression(arg0);
    }

    @Override()
    public MinorThan rightExpression(Expression arg0) {
        return (MinorThan) super.rightExpression(arg0);
    }

    @Override()
    public MinorThan oldOracleJoinSyntax(int arg0) {
        return (MinorThan) super.oldOracleJoinSyntax(arg0);
    }

    @Override()
    public MinorThan oraclePriorPosition(int arg0) {
        return (MinorThan) super.oraclePriorPosition(arg0);
    }
}
