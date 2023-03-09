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

import net.sf.jsqlparser.expression.*;

import java.io.Serializable;

public class Fetch implements Serializable {
    private Expression expression = null;
    private boolean isFetchParamFirst = false;
    private String fetchParam = "ROW";

    @Deprecated
    public long getRowCount() {
        return expression instanceof LongValue ? ((LongValue) expression).getValue() : null;
    }

    @Deprecated
    public void setRowCount(long l) {
        setExpression(new LongValue(l));
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Fetch withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    @Deprecated
    public JdbcParameter getFetchJdbcParameter() {
        return expression instanceof JdbcParameter ? (JdbcParameter) expression : null;
    }

    public String getFetchParam() {
        return fetchParam;
    }

    public boolean isFetchParamFirst() {
        return isFetchParamFirst;
    }

    @Deprecated
    public void setFetchJdbcParameter(JdbcParameter jdbc) {
        this.setExpression(jdbc);
    }

    public void setFetchParam(String s) {
        this.fetchParam = s;
    }

    public void setFetchParamFirst(boolean b) {
        this.isFetchParamFirst = b;
    }

    @Override
    public String toString() {
        return " FETCH " + (isFetchParamFirst ? "FIRST" : "NEXT") + " " + expression.toString()
                + " " + fetchParam + " ONLY";
    }

    public Fetch withRowCount(long rowCount) {
        this.setRowCount(rowCount);
        return this;
    }

    public Fetch withFetchJdbcParameter(JdbcParameter fetchJdbcParameter) {
        this.setFetchJdbcParameter(fetchJdbcParameter);
        return this;
    }

    public Fetch withFetchParam(String fetchParam) {
        this.setFetchParam(fetchParam);
        return this;
    }
}
