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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.Arrays;
import java.util.List;

public class TimezoneExpression extends ASTNodeAccessImpl implements Expression {

    private final ExpressionList<Expression> timezoneExpressions = new ExpressionList<>();
    private Expression leftExpression;

    public TimezoneExpression() {
        leftExpression = null;
    }

    public TimezoneExpression(Expression leftExpression, Expression... timezoneExpressions) {
        this.leftExpression = leftExpression;
        this.timezoneExpressions.addAll(Arrays.asList(timezoneExpressions));
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public TimezoneExpression setLeftExpression(Expression expression) {
        this.leftExpression = expression;
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public List<Expression> getTimezoneExpressions() {
        return timezoneExpressions;
    }

    public void addTimezoneExpression(Expression... timezoneExpr) {
        this.timezoneExpressions.addAll(Arrays.asList(timezoneExpr));
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder(leftExpression.toString());
        for (Expression expr : timezoneExpressions) {
            returnValue.append(" AT TIME ZONE ").append(expr.toString());
        }

        return returnValue.toString();
    }
}
