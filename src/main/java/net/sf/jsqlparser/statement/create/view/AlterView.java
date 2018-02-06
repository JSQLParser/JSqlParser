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

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * A "CREATE VIEW" statement
 */
@Data
public class AlterView implements Statement {

	/**
	 * In the syntax tree, a view looks and acts just like a Table.
	 */
	private Table view;
	/**
	 * @return the SelectBody
	 */
	private SelectBody selectBody;
	private List<String> columnNames = null;

	@Override
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder sql = new StringBuilder("ALTER ");
		sql.append("VIEW ");
		sql.append(view);
		if (columnNames != null) {
			sql.append(PlainSelect.getStringList(columnNames, true, true));
		}
		sql.append(" AS ").append(selectBody);
		return sql.toString();
	}
}
