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
/*
 * Copyright (C) 2013 toben.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.sf.jsqlparser.statement.select;

import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;

/**
 * This is a container for a values item within a select statement. It holds
 * some syntactical stuff that differs from values within an insert statement.
 *
 * @author toben
 */
public class ValuesList implements FromItem {

	private String alias;
	private MultiExpressionList multiExpressionList;
	private boolean noBrackets = false;
	private List<String> columnNames;

	public ValuesList() {
	}

	public ValuesList(MultiExpressionList multiExpressionList) {
		this.multiExpressionList = multiExpressionList;
	}

	@Override
	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public MultiExpressionList getMultiExpressionList() {
		return multiExpressionList;
	}

	public void setMultiExpressionList(MultiExpressionList multiExpressionList) {
		this.multiExpressionList = multiExpressionList;
	}

	public boolean isNoBrackets() {
		return noBrackets;
	}

	public void setNoBrackets(boolean noBrackets) {
		this.noBrackets = noBrackets;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append("(VALUES ");
		for (Iterator<ExpressionList> it = getMultiExpressionList().getExprList().iterator(); it.hasNext();) {
			b.append(PlainSelect.getStringList(it.next().getExpressions(), true, !isNoBrackets()));
			if (it.hasNext()) {
				b.append(", ");
			}
		}
		b.append(")");
		if (alias != null) {
			b.append(" AS ").append(alias);

			if (columnNames != null) {
				b.append("(");
				for (Iterator<String> it = columnNames.iterator(); it.hasNext();) {
					b.append(it.next());
					if (it.hasNext()) {
						b.append(", ");
					}
				}
				b.append(")");
			}
		}
		return b.toString();
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
}
