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

/**
 * A table created by "(tab1 join tab2)".
 */
public class SubJoin implements FromItem {
	private FromItem left;
	private Join join;
	private String alias;

	@Override
	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}

	public FromItem getLeft() {
		return left;
	}

	public void setLeft(FromItem l) {
		left = l;
	}

	public Join getJoin() {
		return join;
	}

	public void setJoin(Join j) {
		join = j;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String string) {
		alias = string;
	}

	@Override
	public String toString() {
		return "(" + left + " " + join + ")" + ((alias != null) ? " AS " + alias : "");
	}
}
