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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.Objects;

public class IntervalExpression extends ASTNodeAccessImpl implements Expression {

    private String parameter = null;
    private String intervalType = null;
    private final boolean intervalKeyword;
    private Expression expression = null;

    public IntervalExpression() {
        this(true);
    }

    public IntervalExpression(boolean intervalKeyword) {
        this.intervalKeyword = intervalKeyword;
    }

    public boolean isUsingIntervalKeyword() {
        return intervalKeyword;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return (intervalKeyword ? "INTERVAL " : "")
                + Objects.toString(expression, parameter)
                + (intervalType != null ? " " + intervalType : "");
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public IntervalExpression withParameter(String parameter) {
        this.setParameter(parameter);
        return this;
    }

    public IntervalExpression withIntervalType(String intervalType) {
        this.setIntervalType(intervalType);
        return this;
    }

    public IntervalExpression withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }
}
