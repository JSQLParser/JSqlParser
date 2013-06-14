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
package net.sf.jsqlparser.statement.update;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

/**
 * The update statement.
 */
public class Update implements Statement {

	private Table table;
	private Expression where;
	private List<Column> columns;
	private List<Expression> expressions;
	private FromItem fromItem;
	private List<Join> joins;

	@Override
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	public Table getTable() {
		return table;
	}

	public Expression getWhere() {
		return where;
	}

	public void setTable(Table name) {
		table = name;
	}

	public void setWhere(Expression expression) {
		where = expression;
	}

	/**
	 * The {@link net.sf.jsqlparser.schema.Column}s in this update (as col1 and
	 * col2 in UPDATE col1='a', col2='b')
	 *
	 * @return a list of {@link net.sf.jsqlparser.schema.Column}s
	 */
	public List<Column> getColumns() {
		return columns;
	}

	/**
	 * The {@link Expression}s in this update (as 'a' and 'b' in UPDATE
	 * col1='a', col2='b')
	 *
	 * @return a list of {@link Expression}s
	 */
	public List<Expression> getExpressions() {
		return expressions;
	}

	public void setColumns(List<Column> list) {
		columns = list;
	}

	public void setExpressions(List<Expression> list) {
		expressions = list;
	}

	public FromItem getFromItem() {
		return fromItem;
	}

	public void setFromItem(FromItem fromItem) {
		this.fromItem = fromItem;
	}

	public List<Join> getJoins() {
		return joins;
	}

	public void setJoins(List<Join> joins) {
		this.joins = joins;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("UPDATE ");
		b.append(getTable()).append(" SET ");
		for (int i = 0; i < getColumns().size(); i++) {
			if (i != 0) {
				b.append(", ");
			}
			b.append(columns.get(i)).append(" = ");
			b.append(expressions.get(i));
		}

		if (fromItem != null) {
			b.append(" FROM ").append(fromItem);
			if (joins != null) {
				for (Join join : joins) {
					if (join.isSimple()) {
						b.append(", ").append(join);
					} else {
						b.append(" ").append(join);
					}
				}
			}
		}

		if (where != null) {
			b.append(" WHERE ");
			b.append(where);
		}
		return b.toString();
	}
}
