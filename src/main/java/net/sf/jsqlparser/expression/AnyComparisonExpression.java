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
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * Combines ANY and SOME expressions.
 *
 * @author toben
 */
public class AnyComparisonExpression implements Expression {

    private final SubSelect subSelect;
    private final AnyType anyType;

    public AnyComparisonExpression(AnyType anyType, SubSelect subSelect) {
        this.anyType = anyType;
        this.subSelect = subSelect;
    }

    public SubSelect getSubSelect() {
        return subSelect;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public AnyType getAnyType() {
        return anyType;
    }

    @Override
    public String toString() {
        return anyType.name() + " " + subSelect.toString();
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
