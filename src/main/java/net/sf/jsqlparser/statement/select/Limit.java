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
 * A limit clause in the form [LIMIT {[offset,] row_count) | (row_count | ALL)
 * OFFSET offset}]
 * or in the form [OFFSET offset (ROW | ROWS) [FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY]]
 * or in the form FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
 */
public class Limit {

	private long offset;
	private long rowCount;
	private boolean rowCountJdbcParameter = false;
	private boolean offsetJdbcParameter = false;
	private boolean limitAll;
    private boolean limitNull = false;
    private boolean oracleSqlServerVersion = false;
    private boolean hasOffset = false;
	private boolean isOffsetParamRows = false;
    private boolean hasFetch = false;
    private boolean isFetchParamRows = false;
    private boolean isFetchParamFirst = false;

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

	public boolean isOffsetJdbcParameter() {
		return offsetJdbcParameter;
	}

	public boolean isRowCountJdbcParameter() {
		return rowCountJdbcParameter;
	}

	public boolean isOracleSqlServerVersion() {
		return oracleSqlServerVersion;
	}

	public boolean isHasOffset() {
		return hasOffset;
	}

	public boolean isHasFetch() {
		return hasFetch;
	}
    public boolean isOffsetParamRows() {
		return isOffsetParamRows;
	}

	public boolean isFetchParamRows() {
		return isFetchParamRows;
	}

	public boolean isFetchParamFirst() {
		return isFetchParamFirst;
	}

	public void setOffsetJdbcParameter(boolean b) {
		offsetJdbcParameter = b;
	}

	public void setRowCountJdbcParameter(boolean b) {
		rowCountJdbcParameter = b;
	}

	public void setOracleSqlServerVersion(boolean b) {
		oracleSqlServerVersion = b;
	}

	public void setHasOffset(boolean b) {
		hasOffset = b;
	}

	public void setHasFetch(boolean b) {
		hasFetch = b;
	}

	public void setOffsetParamRows(boolean isOffsetParamRows) {
		this.isOffsetParamRows = isOffsetParamRows;
	}

	public void setFetchParamRows(boolean isFetchParamRows) {
		this.isFetchParamRows = isFetchParamRows;
	}

	public void setFetchParamFirst(boolean isFetchParamFirst) {
		this.isFetchParamFirst = isFetchParamFirst;
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
		String retVal = "";
		if (oracleSqlServerVersion) {
			if (hasOffset) {
				retVal = " OFFSET " + offset + " "+(isOffsetParamRows ? "ROWS" : "ROW");
			}
			if (hasFetch) {
				retVal += " FETCH "+(isFetchParamFirst ? "FIRST" : "NEXT")+" " + rowCount + " "+(isFetchParamRows ? "ROWS" : "ROW")+" ONLY";
			}
		} else {
			if (limitNull) {
	            retVal += " LIMIT NULL";
	        } else if (rowCount >= 0 || rowCountJdbcParameter) {
				retVal += " LIMIT " + (rowCountJdbcParameter ? "?" : rowCount + "");
			}
			if (offset > 0 || offsetJdbcParameter) {
				retVal += " OFFSET " + (offsetJdbcParameter ? "?" : offset + "");
			}
		}
		return retVal;
	}
}
