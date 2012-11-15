package net.sf.jsqlparser.statement.select;

/**
 * A limit clause in the form [LIMIT {[offset,] row_count) | (row_count | ALL) OFFSET offset}]
 */
public class Limit {
	private long offset;
	private long rowCount;
	private boolean rowCountJdbcParameter = false;
	private boolean offsetJdbcParameter = false;
	private boolean limitAll;

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

	public void setOffsetJdbcParameter(boolean b) {
		offsetJdbcParameter = b;
	}

	public void setRowCountJdbcParameter(boolean b) {
		rowCountJdbcParameter = b;
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

	@Override
	public String toString() {
		String retVal = "";
		if (rowCount > 0 || rowCountJdbcParameter) {
			retVal += " LIMIT " + (rowCountJdbcParameter ? "?" : rowCount + "");
		}
		if (offset > 0 || offsetJdbcParameter) {
			retVal += " OFFSET " + (offsetJdbcParameter ? "?" : offset + "");
		}
		return retVal;
	}
}
