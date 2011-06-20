package net.sf.jsqlparser.statement.select;

/**
 * A top clause in the form [TOP row_count]
 */
public class Top {
	private long rowCount;
	private boolean rowCountJdbcParameter = false;

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long l) {
		rowCount = l;
	}

	public boolean isRowCountJdbcParameter() {
		return rowCountJdbcParameter;
	}

	public void setRowCountJdbcParameter(boolean b) {
		rowCountJdbcParameter = b;
	}

	public String toString() {
		return "TOP " + (rowCountJdbcParameter ? "?" : rowCount + "");
	}

}
