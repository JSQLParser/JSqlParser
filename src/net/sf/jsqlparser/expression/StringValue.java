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

package net.sf.jsqlparser.expression;

/**
 * A string as in 'example_string'
 */
public class StringValue implements Expression {
	private String value = "";

	public StringValue(String escapedValue) {
		// romoving "'" at the start and at the end
		value = escapedValue.substring(1, escapedValue.length() - 1);
	}

	public String getValue() {
		return value;
	}

	public String getNotExcapedValue() {
		StringBuffer buffer = new StringBuffer(value);
		int index = 0;
		int deletesNum = 0;
		while ((index = value.indexOf("''", index)) != -1) {
			buffer.deleteCharAt(index - deletesNum);
			index += 2;
			deletesNum++;
		}
		return buffer.toString();
	}

	public void setValue(String string) {
		value = string;
	}

	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String toString() {
		return "'" + value + "'";
	}
}
