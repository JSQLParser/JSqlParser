/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2016 JSQLParser
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

public class TimeKeyExpression implements Expression {

    private String stringValue;

    public TimeKeyExpression(final String value) {
        this.stringValue = value;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String string) {
        stringValue = string;
    }

    @Override
    public String toString() {
        return getStringValue();
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
    public String getLogicalType() {
        return expressionType;
    }

}
