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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

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

    public boolean isLimitAll() {
        return limitAll;
    }

    public void setLimitAll(boolean b) {
        limitAll = b;
    }

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
            if (limitAll) {
                retVal += "ALL";
            } else {
                if (null != offset) {
                    retVal += offset + ", ";
                }
                if (null != rowCount) {
                    retVal += rowCount;
                }
            }
        }

        return retVal;
    }
}
