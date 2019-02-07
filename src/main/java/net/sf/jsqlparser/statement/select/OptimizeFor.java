package net.sf.jsqlparser.statement.select;

/**
 * A optimize for clause.
 */
public class OptimizeFor {

    private long rowCount;

    public OptimizeFor(long rowCount) {
        this.rowCount = rowCount;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long l) {
        rowCount = l;
    }

    @Override
    public String toString() {
        return " OPTIMIZE FOR " + rowCount + " ROWS";
    }
}
