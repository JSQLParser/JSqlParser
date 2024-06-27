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

public class ExistsExpression extends ASTNodeAccessImpl implements Expression {

    protected Expression rightExpression;
    protected boolean not = false;

    public Expression getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(Expression expression) {
        rightExpression = expression;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean b) {
        not = b;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public String getStringExpression() {
        return (not ? "NOT " : "") + "EXISTS";
    }

    @Override
    public String toString() {
        return getStringExpression() + " " + rightExpression.toString();
    }

    public ExistsExpression withRightExpression(Expression rightExpression) {
        this.setRightExpression(rightExpression);
        return this;
    }

    public ExistsExpression withNot(boolean not) {
        this.setNot(not);
        return this;
    }

    public <E extends Expression> E getRightExpression(Class<E> type) {
        return type.cast(getRightExpression());
    }
}
