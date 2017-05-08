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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * An array literal is a list of simple expressions
 * <p>
 * This shows up in Postgres SQL as
 * <pre>
 * ARRAY[val1, val2, val3, ...]
 * </pre>
 */
public class ArrayLiteral implements Expression {

	private ExpressionList list;

	public ArrayLiteral() {
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public ExpressionList getList() {
		return list;
	}

	public void setList(ExpressionList list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "ARRAY["+list.toStringNoBrackets()+"]";
	}
}
