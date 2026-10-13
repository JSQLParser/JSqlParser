/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class RangeExpression extends ASTNodeAccessImpl implements Expression {
    private Expression startExpression;
    private Expression endExpression;
    private Expression stepExpression;

    public RangeExpression(Expression startExpression, Expression endExpression) {
        this.startExpression = startExpression;
        this.endExpression = endExpression;
    }

    public Expression getStartExpression() {
        return startExpression;
    }

    public RangeExpression setStartExpression(Expression startExpression) {
        this.startExpression = startExpression;
        return this;
    }

    public Expression getEndExpression() {
        return endExpression;
    }

    public RangeExpression setEndExpression(Expression endExpression) {
        this.endExpression = endExpression;
        return this;
    }

    public Expression getStepExpression() {
        return stepExpression;
    }

    public RangeExpression setStepExpression(Expression stepExpression) {
        this.stepExpression = stepExpression;
        return this;
    }

    public RangeExpression withStepExpression(Expression stepExpression) {
        return setStepExpression(stepExpression);
    }

    /**
     * Renders {@code [start]:[end][:step]}, a missing end before a step as DuckDB's {@code -}.
     */
    public static String toString(Expression start, Expression end, Expression step) {
        StringBuilder builder = new StringBuilder();
        if (start != null) {
            builder.append(start);
        }
        builder.append(":");
        if (end != null) {
            builder.append(end);
        } else if (step != null) {
            builder.append("-");
        }
        if (step != null) {
            builder.append(":").append(step);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(startExpression, endExpression, stepExpression);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}
