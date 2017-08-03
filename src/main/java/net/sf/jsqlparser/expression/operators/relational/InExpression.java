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
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class InExpression implements Expression, SupportsOldOracleJoinSyntax {

    private Expression leftExpression;
    private ItemsList leftItemsList;
    private ItemsList rightItemsList;
    private boolean not = false;

    private int oldOracleJoinSyntax = NO_ORACLE_JOIN;

    public InExpression() {
    }

    public InExpression(Expression leftExpression, ItemsList itemsList) {
        setLeftExpression(leftExpression);
        setRightItemsList(itemsList);
    }

    @Override
    public void setOldOracleJoinSyntax(int oldOracleJoinSyntax) {
        this.oldOracleJoinSyntax = oldOracleJoinSyntax;
        if (oldOracleJoinSyntax < 0 || oldOracleJoinSyntax > 1) {
            throw new IllegalArgumentException("unexpected join type for oracle found with IN (type=" + oldOracleJoinSyntax + ")");
        }
    }

    @Override
    public int getOldOracleJoinSyntax() {
        return oldOracleJoinSyntax;
    }

    public ItemsList getRightItemsList() {
        return rightItemsList;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public final void setRightItemsList(ItemsList list) {
        rightItemsList = list;
    }

    public final void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean b) {
        not = b;
    }

    public ItemsList getLeftItemsList() {
        return leftItemsList;
    }

    public void setLeftItemsList(ItemsList leftItemsList) {
        this.leftItemsList = leftItemsList;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    private String getLeftExpressionString() {
        return leftExpression + (oldOracleJoinSyntax == ORACLE_JOIN_RIGHT ? "(+)" : "");
    }

    @Override
    public String toString() {
        return (leftExpression == null ? leftItemsList : getLeftExpressionString()) + " " + (not ? "NOT " : "") + "IN " + rightItemsList + "";
    }

    @Override
    public int getOraclePriorPosition() {
        return SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR;
    }

    @Override
    public void setOraclePriorPosition(int priorPosition) {
        if (priorPosition != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR) {
            throw new IllegalArgumentException("unexpected prior for oracle found");
        }
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
