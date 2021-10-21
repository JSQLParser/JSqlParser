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

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class Limit extends ASTNodeAccessImpl {

    private Expression rowCount;
    private Expression offset;

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

    @Deprecated
    public boolean isLimitAll() {
        return rowCount instanceof AllValue;
    }

    @Deprecated
    public void setLimitAll(boolean b) {
        if (b) {
            rowCount = new AllValue();
        }
    }

    @Deprecated
    public boolean isLimitNull() {
        return rowCount instanceof NullValue;
    }

    @Deprecated
    public void setLimitNull(boolean b) {
        if (b) {
            rowCount = new NullValue();
        }
    }

    @Override
    public String toString() {
        String retVal = " LIMIT ";

        if (rowCount instanceof AllValue || rowCount instanceof NullValue) {
            // no offset allowed
            retVal += rowCount;
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

    public Limit withRowCount(Expression rowCount) {
        this.setRowCount(rowCount);
        return this;
    }

    public Limit withOffset(Expression offset) {
        this.setOffset(offset);
        return this;
    }

    @Deprecated
    public Limit withLimitAll(boolean limitAll) {
        this.setLimitAll(limitAll);
        return this;
    }

    @Deprecated
    public Limit withLimitNull(boolean limitNull) {
        this.setLimitNull(limitNull);
        return this;
    }

    public <E extends Expression> E getOffset(Class<E> type) {
        return type.cast(getOffset());
    }

    public <E extends Expression> E getRowCount(Class<E> type) {
        return type.cast(getRowCount());
    }
}
