/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2018 JSQLParser
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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class SubstringExpression extends ASTNodeAccessImpl implements Expression {

    private Expression expression;
    private Expression start;
    private Expression length;

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getStart() {
        return start;
    }

    public void setStart(Expression start) {
        this.start = start;
    }

    public Expression getLength() {
        return length;
    }

    public void setLength(Expression length) {
        this.length = length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SUBSTRING(");
        sb.append(expression);
        sb.append(" FROM ");
        sb.append(start);
        if (length != null) {
            sb.append(" FOR ");
            sb.append(length);
        }
        sb.append(")");
        return sb.toString();
    }
}
