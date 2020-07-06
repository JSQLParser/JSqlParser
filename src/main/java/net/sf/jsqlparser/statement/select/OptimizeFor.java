/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
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

    public OptimizeFor withRowCount(long rowCount) {
        this.setRowCount(rowCount);
        return this;
    }
}
