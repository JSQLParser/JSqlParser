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

public class First {

    public enum Keyword {
        FIRST,
        LIMIT
    }

    private Keyword keyword;
    private Long rowCount;
    private JdbcParameter jdbcParameter;
    private String variable;

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public JdbcParameter getJdbcParameter() {
        return jdbcParameter;
    }

    public void setJdbcParameter(JdbcParameter jdbcParameter) {
        this.jdbcParameter = jdbcParameter;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        String result = keyword.name() + " ";

        if (rowCount != null) {
            result += rowCount;
        } else if (jdbcParameter != null) {
            result += jdbcParameter.toString();
        } else if (variable != null) {
            result += variable;
        }

        return result;
    }
}
