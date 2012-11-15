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

package net.sf.jsqlparser.schema;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

/**
 * A column. It can have the table name it belongs to.
 */
public class Column implements Expression {
	private String columnName = "";
	private Table table;

	public Column() {
	}

	public Column(Table table, String columnName) {
		this.table = table;
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public Table getTable() {
		return table;
	}

	public void setColumnName(String string) {
		columnName = string;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	/**
	 * @return the name of the column, prefixed with 'tableName' and '.'
	 */
	public String getWholeColumnName() {

		String columnWholeName = null;
		String tableWholeName = table.getWholeTableName();

		if (tableWholeName != null && tableWholeName.length() != 0) {
			columnWholeName = tableWholeName + "." + columnName;
		} else {
			columnWholeName = columnName;
		}

		return columnWholeName;

	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return getWholeColumnName();
	}
}
