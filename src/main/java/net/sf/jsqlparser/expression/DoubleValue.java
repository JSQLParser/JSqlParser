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
 * Every number with a point or a exponential format is a DoubleValue
 */
public class DoubleValue implements Expression {

	private double value;
	private String stringValue;

	public DoubleValue(final String value) {
		String val = value;
		if (val.charAt(0) == '+') {
			val = val.substring(1);
		}
		this.value = Double.parseDouble(val);
		this.stringValue = val;
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double d) {
		value = d;
	}

	@Override
	public String toString() {
		return stringValue;
	}
}
