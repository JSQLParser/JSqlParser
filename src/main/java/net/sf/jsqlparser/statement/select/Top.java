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
package net.sf.jsqlparser.statement.select;

import lombok.Data;
import net.sf.jsqlparser.expression.Expression;

/**
 * A top clause in the form [TOP (row_count) or TOP row_count]
 */
@Data
public class Top {

	private boolean parenthesis = false;
	private boolean percentage = false;
	private Expression expression;

	@Override
	public String toString() {
		String result = "TOP ";

		if (parenthesis) {
			result += "(";
		}

		result += expression.toString();

		if (parenthesis) {
			result += ")";
		}

		if (percentage) {
			result += " PERCENT";
		}

		return result;
	}
}
