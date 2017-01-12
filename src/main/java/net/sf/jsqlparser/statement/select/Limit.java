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

import net.sf.jsqlparser.expression.JdbcParameter;

/**
 * A limit clause in the form [LIMIT {[offset,] row_count) | (row_count | ALL)
 * OFFSET offset}]
 */
public class Limit {

	private long offset;
	private long rowCount;
	private JdbcParameter rowCountJdbcParameter;
	private JdbcParameter offsetJdbcParameter;
	private boolean limitAll;
    private boolean limitNull = false;

	public long getOffset() {
		return offset;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setOffset(long l) {
		offset = l;
	}

	public void setRowCount(long l) {
		rowCount = l;
	}

	public JdbcParameter getRowCountJdbcParameter() {
		return rowCountJdbcParameter;
	}

	public void setRowCountJdbcParameter(JdbcParameter rowCountJdbcParameter) {
		this.rowCountJdbcParameter = rowCountJdbcParameter;
	}

	public JdbcParameter getOffsetJdbcParameter() {
		return offsetJdbcParameter;
	}

	public void setOffsetJdbcParameter(JdbcParameter offsetJdbcParameter) {
		this.offsetJdbcParameter = offsetJdbcParameter;
	}

	public boolean isRowCountJdbcParameter(){
		return null != this.rowCountJdbcParameter;
	}

	public boolean isOffsetJdbcParameter(){
		return null != this.offsetJdbcParameter;
	}

	/**
	 * @return true if the limit is "LIMIT ALL [OFFSET ...])
	 */
	public boolean isLimitAll() {
		return limitAll;
	}

	public void setLimitAll(boolean b) {
		limitAll = b;
	}

    /**
     * @return true if the limit is "LIMIT NULL [OFFSET ...])
     */
    public boolean isLimitNull() { return limitNull; }

    public void setLimitNull(boolean b) { limitNull = b; }

	@Override
	public String toString() {
		String retVal = " LIMIT ";
		if (limitNull) {
            retVal += "NULL";
        } else {
            if (offset > 0 || null != offsetJdbcParameter) {
                retVal += (null != offsetJdbcParameter ? offsetJdbcParameter : Long.toString(offset)) + ", ";
    		}
            if ( rowCount >= 0 || null != rowCountJdbcParameter) {
                retVal += (null !=rowCountJdbcParameter ? rowCountJdbcParameter : Long.toString(rowCount));
            }
        }
		
		return retVal;
	}
}
