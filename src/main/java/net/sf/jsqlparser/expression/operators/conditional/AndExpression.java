/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.conditional;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class AndExpression extends BinaryExpression {
    private boolean useOperator = false;

    public AndExpression() {
        // nothing
    }

    public AndExpression(Expression leftExpression, Expression rightExpression) {
        setLeftExpression(leftExpression);
        setRightExpression(rightExpression);
    }

    public void setUseOperator(boolean useOperator) {
        this.useOperator = useOperator;
    }

    public boolean isUseOperator() {
        return useOperator;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return useOperator ? "&&" : "AND";
    }

    public static AndExpression create(Expression leftExpression, Expression rightExpression) {
        return new AndExpression(leftExpression, rightExpression);
    }

    public AndExpression withUseOperator(boolean useOperator) {
        this.setUseOperator(useOperator);
        return this;
    }

    @Override()
    public AndExpression withLeftExpression(Expression arg0) {
        return (AndExpression) super.withLeftExpression(arg0);
    }

    @Override()
    public AndExpression withRightExpression(Expression arg0) {
        return (AndExpression) super.withRightExpression(arg0);
    }
}
