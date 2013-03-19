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
package net.sf.jsqlparser.statement.create.view;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * A "CREATE VIEW" statement
 */
public class CreateView implements Statement {

	private Table view;
	private SelectBody selectBody;
	private boolean orReplace = false;

	@Override
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	/**
	 * In the syntax tree, a view looks and acts just like a Table.
	 *
	 * @return The name of the view to be created.
	 */
	public Table getView() {
		return view;
	}

	public void setView(Table view) {
		this.view = view;
	}

	/**
	 * @return was "OR REPLACE" specified?
	 */
	public boolean isOrReplace() {
		return orReplace;
	}

	/**
	 * @param orReplace was "OR REPLACE" specified?
	 */
	public void setOrReplace(boolean orReplace) {
		this.orReplace = orReplace;
	}

	/**
	 * @return the SelectBody
	 */
	public SelectBody getSelectBody() {
		return selectBody;
	}

	public void setSelectBody(SelectBody selectBody) {
		this.selectBody = selectBody;
	}

	@Override
	public String toString() {
		String sql = "CREATE ";
		if (isOrReplace()) {
			sql += "OR REPLACE ";
		}
		sql += "VIEW " + view + " AS " + selectBody;
		return sql;
	}
}
