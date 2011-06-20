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

package net.sf.jsqlparser.statement.replace;

import java.util.List;

import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * The replace statement.
 */
public class Replace implements Statement {
	private Table table;
	private List columns;
	private ItemsList itemsList;
	private List expressions;
	private boolean useValues = true;

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
	 * A list of {@link net.sf.jsqlparser.schema.Column}s either from a "REPLACE mytab (col1, col2) [...]" or a
	 * "REPLACE mytab SET col1=exp1, col2=exp2".
	 * 
	 * @return a list of {@link net.sf.jsqlparser.schema.Column}s
	 */
	public List getColumns() {
		return columns;
	}

	/**
	 * An {@link ItemsList} (either from a "REPLACE mytab VALUES (exp1,exp2)" or a "REPLACE mytab SELECT * FROM mytab2")
	 * it is null in case of a "REPLACE mytab SET col1=exp1, col2=exp2"
	 */
	public ItemsList getItemsList() {
		return itemsList;
	}

	public void setColumns(List list) {
		columns = list;
	}

	public void setItemsList(ItemsList list) {
		itemsList = list;
	}

	/**
	 * A list of {@link net.sf.jsqlparser.expression.Expression}s (from a "REPLACE mytab SET col1=exp1, col2=exp2"). <br>
	 * it is null in case of a "REPLACE mytab (col1, col2) [...]"
	 */
	public List getExpressions() {
		return expressions;
	}

	public void setExpressions(List list) {
		expressions = list;
	}

	public boolean isUseValues() {
		return useValues;
	}

	public void setUseValues(boolean useValues) {
		this.useValues = useValues;
	}

	public String toString() {
		String sql = "REPLACE " + table;

		if (expressions != null && columns != null) {
			// the SET col1=exp1, col2=exp2 case
			sql += " SET ";
			// each element from expressions match up with a column from columns.
			for (int i = 0, s = columns.size(); i < s; i++) {
				sql += "" + columns.get(i) + "=" + expressions.get(i);
				sql += (i < s - 1) ? ", " : "";
			}
		} else if (columns != null) {
			// the REPLACE mytab (col1, col2) [...] case
			sql += " " + PlainSelect.getStringList(columns, true, true);
		}

		if (itemsList != null) {
			// REPLACE mytab SELECT * FROM mytab2
			// or VALUES ('as', ?, 565)

			if (useValues) {
				sql += " VALUES";
			}

			sql += " " + itemsList;
		}

		return sql;
	}

}
