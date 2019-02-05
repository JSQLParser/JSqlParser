/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A "BETWEEN" expr1 expr2 statement
 */
public class Between extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private boolean not = false;
    private Expression betweenExpressionStart;
    private Expression betweenExpressionEnd;

    public Expression getBetweenExpressionEnd() {
        return betweenExpressionEnd;
    }

    public Expression getBetweenExpressionStart() {
        return betweenExpressionStart;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public boolean isNot() {
        return not;
    }

    public void setBetweenExpressionEnd(Expression expression) {
        betweenExpressionEnd = expression;
    }

    public void setBetweenExpressionStart(Expression expression) {
        betweenExpressionStart = expression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public void setNot(boolean b) {
        not = b;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return leftExpression + " " + (not ? "NOT " : "") + "BETWEEN " + betweenExpressionStart + " AND "
                + betweenExpressionEnd;
    }
}
