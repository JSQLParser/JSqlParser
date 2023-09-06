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
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fetch implements Serializable {
    private Expression expression = null;
    private boolean isFetchParamFirst = false;
    private final List<String> fetchParameters = new ArrayList<>();

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

    public Fetch addFetchParameter(String parameter) {
        fetchParameters.add(parameter);
        return this;
    }

    public List<String> getFetchParameters() {
        return this.fetchParameters;
    }

    @Deprecated
    public String getFetchParam() {
        String parameterStr = "";
        for (String p : fetchParameters) {
            parameterStr += " " + p;
        }
        return parameterStr.trim();
    }

    public boolean isFetchParamFirst() {
        return isFetchParamFirst;
    }

    @Deprecated
    public void setFetchJdbcParameter(JdbcParameter jdbc) {
        this.setExpression(jdbc);
    }

    @Deprecated
    public void setFetchParam(String s) {
        fetchParameters.clear();
        if (s != null) {
            fetchParameters.addAll(Arrays.asList(s.trim().split("\\s+")));
        }
    }

    public void setFetchParamFirst(boolean b) {
        this.isFetchParamFirst = b;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" FETCH");
        if (isFetchParamFirst) {
            builder.append(" FIRST");
        } else {
            builder.append(" NEXT");
        }

        if (expression != null) {
            builder.append(" ").append(expression);
        }

        for (String s : fetchParameters) {
            builder.append(" ").append(s);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Deprecated
    public Fetch withRowCount(long rowCount) {
        this.setRowCount(rowCount);
        return this;
    }

    @Deprecated
    public Fetch withFetchJdbcParameter(JdbcParameter fetchJdbcParameter) {
        this.setFetchJdbcParameter(fetchJdbcParameter);
        return this;
    }

    @Deprecated
    public Fetch withFetchParam(String fetchParam) {
        this.setFetchParam(fetchParam);
        return this;
    }
}
