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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A "BETWEEN" expr1 expr2 statement
 */
public class Between extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private boolean not = false;
    private Expression betweenExpressionStart;
    private Expression betweenExpressionEnd;
    private boolean usingSymmetric = false;
    private boolean usingAsymmetric = false;

    public Expression getBetweenExpressionEnd() {
        return betweenExpressionEnd;
    }

    public void setBetweenExpressionEnd(Expression expression) {
        betweenExpressionEnd = expression;
    }

    public Expression getBetweenExpressionStart() {
        return betweenExpressionStart;
    }

    public void setBetweenExpressionStart(Expression expression) {
        betweenExpressionStart = expression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean b) {
        not = b;
    }

    public boolean isUsingSymmetric() {
        return usingSymmetric;
    }

    public Between setUsingSymmetric(boolean usingSymmetric) {
        this.usingSymmetric = usingSymmetric;
        return this;
    }

    public boolean isUsingAsymmetric() {
        return usingAsymmetric;
    }

    public Between setUsingAsymmetric(boolean usingAsymmetric) {
        this.usingAsymmetric = usingAsymmetric;
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return leftExpression + " " + (not ? "NOT " : "") + "BETWEEN "
                + (usingSymmetric ? "SYMMETRIC " : "") + (usingAsymmetric ? "ASYMMETRIC " : "")
                + betweenExpressionStart
                + " AND "
                + betweenExpressionEnd;
    }

    public Between withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public Between withNot(boolean not) {
        this.setNot(not);
        return this;
    }

    public Between withBetweenExpressionStart(Expression betweenExpressionStart) {
        this.setBetweenExpressionStart(betweenExpressionStart);
        return this;
    }

    public Between withBetweenExpressionEnd(Expression betweenExpressionEnd) {
        this.setBetweenExpressionEnd(betweenExpressionEnd);
        return this;
    }

    public <E extends Expression> E getBetweenExpressionEnd(Class<E> type) {
        return type.cast(getBetweenExpressionEnd());
    }

    public <E extends Expression> E getBetweenExpressionStart(Class<E> type) {
        return type.cast(getBetweenExpressionStart());
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }
}
