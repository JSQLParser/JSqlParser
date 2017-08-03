/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.genericClass;

/**
 *
 * @author toben
 */
public class OracleHierarchicalExpression implements Expression {

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

    public String expressionType = "None";

    @Override
    public void setExpressionType(String inputExpressionType) {
        expressionType = inputExpressionType;
    }

    @Override
    public String getExpressionType() {
        return expressionType;
    }

    public genericClass logicalType = new genericClass();

    @Override
    public void setLogicalType(Object inputLogicalType) {
        logicalType.setLogicalType(inputLogicalType);
    }

    @Override
    public Object getLogicalType() {
        return logicalType.getLogicalType();
    }
}
