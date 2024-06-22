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

public class OracleHierarchicalExpression extends ASTNodeAccessImpl implements Expression {

    private Expression startExpression;
    private Expression connectExpression;
    private boolean noCycle = false;
    boolean connectFirst = false;

    public Expression getStartExpression() {
        return startExpression;
    }

    public void setStartExpression(Expression startExpression) {
        this.startExpression = startExpression;
    }

    public Expression getConnectExpression() {
        return connectExpression;
    }

    public void setConnectExpression(Expression connectExpression) {
        this.connectExpression = connectExpression;
    }

    public boolean isNoCycle() {
        return noCycle;
    }

    public void setNoCycle(boolean noCycle) {
        this.noCycle = noCycle;
    }

    public boolean isConnectFirst() {
        return connectFirst;
    }

    public void setConnectFirst(boolean connectFirst) {
        this.connectFirst = connectFirst;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (isConnectFirst()) {
            b.append(" CONNECT BY ");
            if (isNoCycle()) {
                b.append("NOCYCLE ");
            }
            b.append(connectExpression.toString());
            if (startExpression != null) {
                b.append(" START WITH ").append(startExpression.toString());
            }
        } else {
            if (startExpression != null) {
                b.append(" START WITH ").append(startExpression.toString());
            }
            b.append(" CONNECT BY ");
            if (isNoCycle()) {
                b.append("NOCYCLE ");
            }
            b.append(connectExpression.toString());
        }
        return b.toString();
    }

    public OracleHierarchicalExpression withStartExpression(Expression startExpression) {
        this.setStartExpression(startExpression);
        return this;
    }

    public OracleHierarchicalExpression withConnectExpression(Expression connectExpression) {
        this.setConnectExpression(connectExpression);
        return this;
    }

    public OracleHierarchicalExpression withNoCycle(boolean noCycle) {
        this.setNoCycle(noCycle);
        return this;
    }

    public OracleHierarchicalExpression withConnectFirst(boolean connectFirst) {
        this.setConnectFirst(connectFirst);
        return this;
    }

    public <E extends Expression> E getStartExpression(Class<E> type) {
        return type.cast(getStartExpression());
    }

    public <E extends Expression> E getConnectExpression(Class<E> type) {
        return type.cast(getConnectExpression());
    }
}
