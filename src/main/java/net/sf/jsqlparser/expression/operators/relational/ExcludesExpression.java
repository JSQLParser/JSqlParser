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

public class ExcludesExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private Expression rightExpression;

    public ExcludesExpression() {}

    public ExcludesExpression(Expression leftExpression, Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public ExcludesExpression withLeftExpression(Expression expression) {
        this.setLeftExpression(expression);
        return this;
    }

    public final void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder statementBuilder = new StringBuilder();
        statementBuilder.append(leftExpression);

        statementBuilder.append(" ");
        statementBuilder.append("EXCLUDES ");

        statementBuilder.append(rightExpression);
        return statementBuilder.toString();
    }

    public ExcludesExpression withRightExpression(Expression rightExpression) {
        this.setRightExpression(rightExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }

    public <E extends Expression> E getRightExpression(Class<E> type) {
        return type.cast(getRightExpression());
    }
}
