/* ================================================================
 * JSQLParser : java based sql parser 
 * ================================================================
 *
 * Project Info:  http://jsqlparser.sourceforge.net
 * Project Lead:  Leonardo Francalanci (leoonardoo@yahoo.it);
 *
 * (C) Copyright 2004, by Leonardo Francalanci
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package net.sf.jsqlparser.statement.insert;

import java.util.List;

import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * The insert statement. Every column name in <code>columnNames</code> matches an item in <code>itemsList</code>
 */
public class Insert implements Statement {
	private Table table;
	private List<Column> columns;
	private ItemsList itemsList;
	private boolean useValues = true;

	@Override
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table name) {
		table = name;
	}

	/**
	 * Get the columns (found in "INSERT INTO (col1,col2..) [...]" )
	 * 
	 * @return a list of {@link net.sf.jsqlparser.schema.Column}
	 */
	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> list) {
		columns = list;
	}

	/**
	 * Get the values (as VALUES (...) or SELECT)
	 * 
	 * @return the values of the insert
	 */
	public ItemsList getItemsList() {
		return itemsList;
	}

	public void setItemsList(ItemsList list) {
		itemsList = list;
	}

	public boolean isUseValues() {
		return useValues;
	}

	public void setUseValues(boolean useValues) {
		this.useValues = useValues;
	}

	@Override
	public String toString() {
		String sql = "";

		sql = "INSERT INTO ";
		sql += table + " ";
		sql += ((columns != null) ? PlainSelect.getStringList(columns, true, true) + " " : "");

		if (useValues) {
			sql += "VALUES " + itemsList + "";
		} else {
			sql += "" + itemsList + "";
		}

		return sql;
	}

}
