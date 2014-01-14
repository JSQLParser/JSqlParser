/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
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
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Utility function for select statements.
 *
 * @author toben
 */
public final class SelectUtils {

	private SelectUtils() {
	}

	/**
	 * Adds an expression to select statements. E.g. a simple column is an expression.
	 *
	 * @param select
	 * @param expr
	 */
	public static void addExpression(Select select, final Expression expr) {
		select.getSelectBody().accept(new SelectVisitor() {

			@Override
			public void visit(PlainSelect plainSelect) {
				plainSelect.getSelectItems().add(new SelectExpressionItem(expr));
			}

			@Override
			public void visit(SetOperationList setOpList) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public void visit(WithItem withItem) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
	}
}
