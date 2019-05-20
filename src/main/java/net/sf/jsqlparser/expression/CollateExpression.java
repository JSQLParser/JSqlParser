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

public class CollateExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private String collate;

    public CollateExpression(Expression leftExpression, String collate) {
        this.leftExpression = leftExpression;
        this.collate = collate;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }

    @Override
    public String toString() {
        return leftExpression.toString() + " COLLATE " + collate;
    }
}
