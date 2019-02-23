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

public class IntervalExpression extends ASTNodeAccessImpl implements Expression {

    private String parameter = null;
    private String intervalType = null;
    private final boolean intervalKeyword;

    public IntervalExpression() {
        this(true);
    }

    public IntervalExpression(boolean intervalKeyword) {
        this.intervalKeyword = intervalKeyword;
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

    @Override
    public String toString() {
        return (intervalKeyword ? "INTERVAL " : "") + parameter + (intervalType != null ? " " + intervalType : "");
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
