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

public class TrimFunction extends ASTNodeAccessImpl implements Expression {
    public enum TrimSpecification {
        LEADING, TRAILING, BOTH
    }

    private TrimSpecification trimSpecification;
    private Expression expression;
    private Expression fromExpression;
    private boolean isUsingFromKeyword;

    public TrimFunction(TrimSpecification trimSpecification,
            Expression expression,
            Expression fromExpression,
            boolean isUsingFromKeyword) {

        this.trimSpecification = trimSpecification;
        this.expression = expression;
        this.fromExpression = fromExpression;
        this.isUsingFromKeyword = isUsingFromKeyword;
    }

    public TrimFunction() {
        this(null, null, null, false);
    }

    public TrimSpecification getTrimSpecification() {
        return trimSpecification;
    }

    public void setTrimSpecification(TrimSpecification trimSpecification) {
        this.trimSpecification = trimSpecification;
    }

    public TrimFunction withTrimSpecification(TrimSpecification trimSpecification) {
        this.setTrimSpecification(trimSpecification);
        return this;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public TrimFunction withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public Expression getFromExpression() {
        return fromExpression;
    }

    public void setFromExpression(Expression fromExpression) {
        if (fromExpression == null) {
            setUsingFromKeyword(false);
        }
        this.fromExpression = fromExpression;
    }

    public TrimFunction withFromExpression(Expression fromExpression) {
        this.setFromExpression(fromExpression);
        return this;
    }

    public boolean isUsingFromKeyword() {
        return isUsingFromKeyword;
    }

    public void setUsingFromKeyword(boolean useFromKeyword) {
        isUsingFromKeyword = useFromKeyword;
    }

    public TrimFunction withUsingFromKeyword(boolean useFromKeyword) {
        this.setUsingFromKeyword(useFromKeyword);
        return this;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("Trim(");

        if (trimSpecification != null) {
            builder.append(" ").append(trimSpecification.name());
        }

        if (expression != null) {
            builder.append(" ").append(expression);
        }

        if (fromExpression != null) {
            builder
                    .append(isUsingFromKeyword ? " FROM " : ", ")
                    .append(fromExpression);
        }
        builder.append(" )");

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
