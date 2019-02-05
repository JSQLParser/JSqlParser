/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 *
 * @author toben
 */
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
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
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
}
