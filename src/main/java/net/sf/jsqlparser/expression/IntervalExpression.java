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

    private final boolean intervalKeyword;
    private String parameter = null;
    private String intervalType = null;
    private IntervalQualifier intervalQualifier = null;
    private Expression expression = null;

    public IntervalExpression() {
        this(true);
    }

    public IntervalExpression(boolean intervalKeyword) {
        this.intervalKeyword = intervalKeyword;
    }

    public IntervalExpression(int value, String type) {
        this.parameter = null;
        this.intervalKeyword = true;
        this.expression = new LongValue(value);
        this.intervalType = type;
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

    /**
     * Returns the non-standard single-identifier interval type (e.g. MySQL {@code INTERVAL 1 foo}),
     * or {@code null} when a structured {@link IntervalQualifier} is present.
     *
     * @deprecated the structured {@link #getIntervalQualifier()} is the canonical representation.
     *             This legacy field is kept only for the non-standard single-identifier form.
     * @return the legacy interval type, or {@code null} if a qualifier is set
     */
    @Deprecated
    public String getIntervalType() {
        return intervalType;
    }

    /**
     * Sets the non-standard single-identifier interval type (e.g. MySQL {@code INTERVAL 1 foo}) and
     * clears any structured qualifier.
     *
     * @deprecated use {@link #setIntervalQualifier(IntervalQualifier)} for the standard form.
     * @param intervalType the legacy interval type
     */
    @Deprecated
    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
        this.intervalQualifier = null;
    }

    public IntervalQualifier getIntervalQualifier() {
        return intervalQualifier;
    }

    /**
     * Sets the structured interval qualifier and clears the legacy interval type.
     */
    public void setIntervalQualifier(IntervalQualifier intervalQualifier) {
        this.intervalQualifier = intervalQualifier;
        this.intervalType = null;
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
                + (intervalQualifier != null
                        ? " " + intervalQualifier.toString()
                        : (intervalType != null ? " " + intervalType : ""));
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public IntervalExpression withParameter(String parameter) {
        this.setParameter(parameter);
        return this;
    }

    /**
     * @deprecated use {@link #withIntervalQualifier(IntervalQualifier)} for the standard form.
     * @param intervalType the legacy interval type
     * @return this instance
     */
    @Deprecated
    public IntervalExpression withIntervalType(String intervalType) {
        this.setIntervalType(intervalType);
        return this;
    }

    public IntervalExpression withIntervalQualifier(IntervalQualifier intervalQualifier) {
        this.setIntervalQualifier(intervalQualifier);
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
