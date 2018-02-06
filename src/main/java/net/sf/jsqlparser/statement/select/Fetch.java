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
import net.sf.jsqlparser.expression.JdbcParameter;

/**
 * A fetch clause in the form FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
 */
@Data
public class Fetch {

	private long rowCount;
	private JdbcParameter fetchJdbcParameter = null;
	private boolean isFetchParamFirst = false;
	private String fetchParam = "ROW";

	@Override
	public String toString() {
		return " FETCH " + (isFetchParamFirst ? "FIRST" : "NEXT") + " "
			+ (fetchJdbcParameter != null ? fetchJdbcParameter.toString() : Long.toString(rowCount)) + " " + fetchParam + " ONLY";
	}
}
