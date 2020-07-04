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

public class Matches extends OldOracleJoinBinaryExpression {

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return "@@";
    }

    @Override()
    public Matches leftExpression(Expression arg0) {
        return (Matches) super.leftExpression(arg0);
    }

    @Override()
    public Matches rightExpression(Expression arg0) {
        return (Matches) super.rightExpression(arg0);
    }

    @Override()
    public Matches oldOracleJoinSyntax(int oldOracleJoinSyntax) {
        return (Matches) super.oldOracleJoinSyntax(oldOracleJoinSyntax);
    }

    @Override()
    public Matches oraclePriorPosition(int oraclePriorPosition) {
        return (Matches) super.oraclePriorPosition(oraclePriorPosition);
    }
}
