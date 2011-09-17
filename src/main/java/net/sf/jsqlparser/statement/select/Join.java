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

package net.sf.jsqlparser.statement.select;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * A join clause
 */
public class Join {
	private boolean outer = false;
	private boolean right = false;
	private boolean left = false;
	private boolean natural = false;
	private boolean full = false;
	private boolean inner = false;
	private boolean simple = false;
	private FromItem rightItem;
	private Expression onExpression;
	private List<Column> usingColumns;

	/**
	 * Whether is a tab1,tab2 join
	 * 
	 * @return true if is a "tab1,tab2" join
	 */
	public boolean isSimple() {
		return simple;
	}

	public void setSimple(boolean b) {
		simple = b;
	}

	/**
	 * Whether is a "INNER" join
	 * 
	 * @return true if is a "INNER" join
	 */
	public boolean isInner() {
		return inner;
	}

	public void setInner(boolean b) {
		inner = b;
	}

	/**
	 * Whether is a "OUTER" join
	 * 
	 * @return true if is a "OUTER" join
	 */
	public boolean isOuter() {
		return outer;
	}

	public void setOuter(boolean b) {
		outer = b;
	}

	/**
	 * Whether is a "LEFT" join
	 * 
	 * @return true if is a "LEFT" join
	 */
	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean b) {
		left = b;
	}

	/**
	 * Whether is a "RIGHT" join
	 * 
	 * @return true if is a "RIGHT" join
	 */
	public boolean isRight() {
		return right;
	}

	public void setRight(boolean b) {
		right = b;
	}

	/**
	 * Whether is a "NATURAL" join
	 * 
	 * @return true if is a "NATURAL" join
	 */
	public boolean isNatural() {
		return natural;
	}

	public void setNatural(boolean b) {
		natural = b;
	}

	/**
	 * Whether is a "FULL" join
	 * 
	 * @return true if is a "FULL" join
	 */
	public boolean isFull() {
		return full;
	}

	public void setFull(boolean b) {
		full = b;
	}

	/**
	 * Returns the right item of the join
	 */
	public FromItem getRightItem() {
		return rightItem;
	}

	public void setRightItem(FromItem item) {
		rightItem = item;
	}

	/**
	 * Returns the "ON" expression (if any)
	 */
	public Expression getOnExpression() {
		return onExpression;
	}

	public void setOnExpression(Expression expression) {
		onExpression = expression;
	}

	/**
	 * Returns the "USING" list of {@link net.sf.jsqlparser.schema.Column}s (if any)
	 */
	public List<Column> getUsingColumns() {
		return usingColumns;
	}

	public void setUsingColumns(List<Column> list) {
		usingColumns = list;
	}

	public String toString() {
		if (isSimple())
			return "" + rightItem;
		else {
			String type = "";

			if (isRight())
				type += "RIGHT ";
			else if (isNatural())
				type += "NATURAL ";
			else if (isFull())
				type += "FULL ";
			else if (isLeft())
				type += "LEFT ";

			if (isOuter())
				type += "OUTER ";
			else if (isInner())
				type += "INNER ";

			return type + "JOIN " + rightItem + ((onExpression != null) ? " ON " + onExpression + "" : "")
					+ PlainSelect.getFormatedList(usingColumns, "USING", true, true);
		}

	}
}
