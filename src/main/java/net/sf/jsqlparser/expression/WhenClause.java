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

/**
 * A clause of following syntax: WHEN condition THEN expression. Which is part of a CaseExpression.
 *
 * @author Havard Rast Blok
 */
public class WhenClause implements Expression {

    private Expression whenExpression;
    private Expression thenExpression;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    /**
     * @return Returns the thenExpression.
     */
    public Expression getThenExpression() {
        return thenExpression;
    }

    /**
     * @param thenExpression The thenExpression to set.
     */
    public void setThenExpression(Expression thenExpression) {
        this.thenExpression = thenExpression;
    }

    /**
     * @return Returns the whenExpression.
     */
    public Expression getWhenExpression() {
        return whenExpression;
    }

    /**
     * @param whenExpression The whenExpression to set.
     */
    public void setWhenExpression(Expression whenExpression) {
        this.whenExpression = whenExpression;
    }

    @Override
    public String toString() {
        return "WHEN " + whenExpression + " THEN " + thenExpression;
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

    public Object logicalType;

    @Override
    public void setLogicalType(Object inputLogicalType) {
        logicalType = inputLogicalType;
    }

    @Override
    public Object getLogicalType() {
        return expressionType;
    }

}
