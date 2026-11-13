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
import java.util.function.Consumer;

public class TimezoneExpression extends ASTNodeAccessImpl implements Expression {

    private final ExpressionList<Expression> timezoneExpressions = new ExpressionList<>();
    private Expression leftExpression;
    private boolean atLocal;

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

    /**
     * Whether the expression ends with {@code AT LOCAL}, which converts to the session time zone.
     * It follows the {@code AT TIME ZONE} conversions of this expression, if there are any.
     */
    public boolean isAtLocal() {
        return atLocal;
    }

    public TimezoneExpression setAtLocal(boolean atLocal) {
        this.atLocal = atLocal;
        return this;
    }

    /**
     * Appends the expression, rendering the operand and each time zone through the renderer.
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        expressionRenderer.accept(leftExpression);
        for (Expression expr : timezoneExpressions) {
            builder.append(" AT TIME ZONE ");
            expressionRenderer.accept(expr);
        }
        if (atLocal) {
            builder.append(" AT LOCAL");
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
