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
 * It represents an expression like "(" expression ")"
 */
public class Parenthesis extends ASTNodeAccessImpl implements Expression {

    private Expression expression;

    public Parenthesis() {
    }

    public Parenthesis(Expression expression) {
        setExpression(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + expression + ")";
    }

    public static Parenthesis create(Expression expression) {
        return new Parenthesis(expression);
    }

    public Parenthesis expression(Expression expression) {
        this.setExpression(expression);
        return this;
    }
}
