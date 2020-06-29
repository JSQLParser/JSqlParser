/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.Optional;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A basic class for binary expressions, that is expressions having a left member and a right member
 * which are in turn expressions.
 */
public abstract class BinaryExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private Expression rightExpression;
    // private boolean not = false;

    public BinaryExpression() {
    }
    
    public <E extends Expression> E getLeftExpression(Class<E> type) {
      return Optional.ofNullable(leftExpression).map(type::cast).orElseGet(null);
  }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public <E extends Expression> E getRightExpression(Class<E> type) {
        return Optional.ofNullable(rightExpression).map(type::cast).orElseGet(null);
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public BinaryExpression leftExpression(Expression expression) {
        setLeftExpression(expression);
        return this;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public BinaryExpression rightExpression(Expression expression) {
        setRightExpression(expression);
        return this;
    }

    public void setRightExpression(Expression expression) {
        rightExpression = expression;
    }

    // public void setNot() {
    // not = true;
    // }
    //
    // public void removeNot() {
    // not = false;
    // }
    //
    // public boolean isNot() {
    // return not;
    // }
    @Override
    public String toString() {
        return // (not ? "NOT " : "") +
                getLeftExpression() + " " + getStringExpression() + " " + getRightExpression();
    }

    public abstract String getStringExpression();

}
