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

import net.sf.jsqlparser.expression.JdbcParameter;

public class Fetch {

    private long rowCount;
    private JdbcParameter fetchJdbcParameter = null;
    private boolean isFetchParamFirst = false;
    private String fetchParam = "ROW";

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long l) {
        rowCount = l;
    }

    public JdbcParameter getFetchJdbcParameter() {
        return fetchJdbcParameter;
    }

    public String getFetchParam() {
        return fetchParam;
    }

    public boolean isFetchParamFirst() {
        return isFetchParamFirst;
    }

    public void setFetchJdbcParameter(JdbcParameter jdbc) {
        fetchJdbcParameter = jdbc;
    }

    public void setFetchParam(String s) {
        this.fetchParam = s;
    }

    public void setFetchParamFirst(boolean b) {
        this.isFetchParamFirst = b;
    }

    @Override
    public String toString() {
        return " FETCH " + (isFetchParamFirst ? "FIRST" : "NEXT") + " " 
                + (fetchJdbcParameter!=null ? fetchJdbcParameter.toString() : 
                    Long.toString(rowCount)) + " " + fetchParam + " ONLY";
    }
}
