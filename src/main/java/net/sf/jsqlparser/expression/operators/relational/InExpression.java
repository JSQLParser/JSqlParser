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

public class InExpression implements Expression {

	private Expression leftExpression;
	private ItemsList itemsList;
	private boolean not = false;

	public InExpression() {
	}

	public InExpression(Expression leftExpression, ItemsList itemsList) {
		setLeftExpression(leftExpression);
		setItemsList(itemsList);
	}

	public ItemsList getItemsList() {
		return itemsList;
	}

	public Expression getLeftExpression() {
		return leftExpression;
	}

	public void setItemsList(ItemsList list) {
		itemsList = list;
	}

	public void setLeftExpression(Expression expression) {
		leftExpression = expression;
	}

	public boolean isNot() {
		return not;
	}

	public void setNot(boolean b) {
		not = b;
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return leftExpression + " " + ((not) ? "NOT " : "") + "IN " + itemsList + "";
	}
}
