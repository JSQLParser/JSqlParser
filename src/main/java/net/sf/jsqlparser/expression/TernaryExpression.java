/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * The C-style ternary conditional operator {@code condition ? thenExpression : elseExpression},
 * supported for instance by ClickHouse as an alias for {@code if(condition, then, else)}.
 */
public class TernaryExpression extends ASTNodeAccessImpl implements Expression {
    private Expression condition;
    private Expression thenExpression;
    private Expression elseExpression;

    public TernaryExpression() {}

    public TernaryExpression(Expression condition, Expression thenExpression,
            Expression elseExpression) {
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public TernaryExpression setCondition(Expression condition) {
        this.condition = condition;
        return this;
    }

    public Expression getThenExpression() {
        return thenExpression;
    }

    public TernaryExpression setThenExpression(Expression thenExpression) {
        this.thenExpression = thenExpression;
        return this;
    }

    public Expression getElseExpression() {
        return elseExpression;
    }

    public TernaryExpression setElseExpression(Expression elseExpression) {
        this.elseExpression = elseExpression;
        return this;
    }

    @Override
    public String toString() {
        return condition + " ? " + thenExpression + " : " + elseExpression;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}
