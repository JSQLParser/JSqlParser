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

import java.sql.Timestamp;

import lombok.Data;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A Timestamp in the form {ts 'yyyy-mm-dd hh:mm:ss.f . . .'}
 */
@Data
public class TimestampValue extends ASTNodeAccessImpl implements Expression {

	private static final char QUOTATION = '\'';

	private Timestamp value;

	public TimestampValue(String value) {
		if (value == null) {
			throw new java.lang.IllegalArgumentException("null string");
		} else {
			if (value.charAt(0) == QUOTATION) {
				this.value = Timestamp.valueOf(value.substring(1, value.length() - 1));
			} else {
				this.value = Timestamp.valueOf(value.substring(0, value.length()));
			}
		}
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "{ts '" + value + "'}";
	}
}
