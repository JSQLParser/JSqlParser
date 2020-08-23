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
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;

public class Offset {

    private long offset;
    private Expression offsetJdbcParameter = null;
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

    public Expression getOffsetJdbcParameter() {
        return offsetJdbcParameter;
    }

    public void setOffsetJdbcParameter(JdbcParameter jdbc) {
        offsetJdbcParameter = jdbc;
    }
    
    public void setOffsetJdbcParameter(JdbcNamedParameter jdbc) {
        offsetJdbcParameter = jdbc;
    }

    @Override
    public String toString() {
        return " OFFSET " + (offsetJdbcParameter!=null ? offsetJdbcParameter.toString() : offset) + (offsetParam != null ? " " + offsetParam : "");
    }

    public Offset withOffset(long offset) {
        this.setOffset(offset);
        return this;
    }

    public Offset withOffsetParam(String offsetParam) {
        this.setOffsetParam(offsetParam);
        return this;
    }

    public <E extends Expression> E getOffsetJdbcParameter(Class<E> type) {
        return type.cast(getOffsetJdbcParameter());
    }
}
