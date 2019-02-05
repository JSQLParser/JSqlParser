/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A limit clause in the form [LIMIT {[offset,] row_count) | (row_count | ALL) OFFSET offset}]
 */
public class Limit extends ASTNodeAccessImpl {

    private Expression rowCount;
    private Expression offset;
    private boolean limitAll;
    private boolean limitNull = false;

    public Expression getOffset() {
        return offset;
    }

    public Expression getRowCount() {
        return rowCount;
    }

    public void setOffset(Expression l) {
        offset = l;
    }

    public void setRowCount(Expression l) {
        rowCount = l;
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
    public boolean isLimitNull() {
        return limitNull;
    }

    public void setLimitNull(boolean b) {
        limitNull = b;
    }

    @Override
    public String toString() {
        String retVal = " LIMIT ";
        if (limitNull) {
            retVal += "NULL";
        } else {
            if (null != offset) {
                retVal += offset + ", ";
            }
            if (null != rowCount) {
                retVal += rowCount;
            }
        }

        return retVal;
    }
}
