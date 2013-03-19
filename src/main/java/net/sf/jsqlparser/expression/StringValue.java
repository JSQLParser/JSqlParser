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
		StringBuilder buffer = new StringBuilder(value);
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

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "'" + value + "'";
	}
}
