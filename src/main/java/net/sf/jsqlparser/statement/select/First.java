/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2015 JSQLParser
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
import net.sf.jsqlparser.expression.JdbcParameter;

/**
 * A FIRST clause in the form [FIRST row_count] the alternative form [LIMIT row_count] is also
 * supported.
 *
 * Initial implementation was done for informix special syntax:
 * http://www-01.ibm.com/support/knowledgecenter/SSGU8G_12.1.0/com.ibm.sqls.doc/ids_sqs_0156.htm
 */
@Data
public class First {

	public enum Keyword {
		FIRST,
		LIMIT
	}

	private Keyword keyword;
	private Long rowCount;
	private JdbcParameter jdbcParameter;
	private String variable;

	@Override
	public String toString() {
		String result = keyword.name() + " ";

		if (rowCount != null) {
			result += rowCount;
		} else if (jdbcParameter != null) {
			result += jdbcParameter.toString();
		} else if (variable != null) {
			result += variable;
		}

		return result;
	}
}
