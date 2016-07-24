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

/**
 * An offset clause in the form OFFSET offset
 * or in the form OFFSET offset (ROW | ROWS)
 */
public class Offset {

	private long offset;
	private boolean offsetJdbcParameter = false;
	private String offsetParam = null;

	public long getOffset() {
		return offset;
	}

	public String getOffsetParam() {
		return offsetParam;
	}

	public void setOffset(long l) {
		offset = l;
	}

	public void setOffsetParam(String s) {
		offsetParam = s;
	}

	public boolean isOffsetJdbcParameter() {
		return offsetJdbcParameter;
	}

	public void setOffsetJdbcParameter(boolean b) {
		offsetJdbcParameter = b;
	}

	@Override
	public String toString() {
		return " OFFSET " + (offsetJdbcParameter ? "?" : offset) + (offsetParam != null ? " "+offsetParam : "");
	}
}
