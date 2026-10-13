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

/**
 * It represents a "-" or "+" or "~" before an expression
 */
public class SignedExpression extends ASTNodeAccessImpl implements Expression {

    private char sign;
    private Expression expression;

    public SignedExpression() {
        // empty constructor
    }

    public SignedExpression(char sign, Expression expression) {
        setSign(sign);
        setExpression(expression);
    }

    public char getSign() {
        return sign;
    }

    public final void setSign(char sign) {
        this.sign = sign;
        if (sign != '+' && sign != '-' && sign != '~') {
            throw new IllegalArgumentException("illegal sign character, only + - ~ allowed");
        }
    }

    public Expression getExpression() {
        return expression;
    }

    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        String operand = expression.toString();
        return getSign() + (needsSpaceBefore(operand) ? " " : "") + operand;
    }

    /**
     * Whether the sign must be separated from an operand starting with {@code firstChar}: "- -1"
     * must not become the line comment "--1", nor "~ ~a" the LIKE operator "~~".
     */
    public boolean needsSpaceBefore(char firstChar) {
        return (sign == '-' || sign == '~') && firstChar == sign;
    }

    private boolean needsSpaceBefore(String operand) {
        return !operand.isEmpty() && needsSpaceBefore(operand.charAt(0));
    }

    public SignedExpression withSign(char sign) {
        this.setSign(sign);
        return this;
    }

    public SignedExpression withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }
}
