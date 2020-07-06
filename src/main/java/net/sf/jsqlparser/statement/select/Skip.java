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

public class Skip {

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

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        String result = "SKIP ";

        if (rowCount != null) {
            result += rowCount;
        } else if (jdbcParameter != null) {
            result += jdbcParameter.toString();
        } else if (variable != null) {
            result += variable;
        }

        return result;
    }

    public Skip withRowCount(Long rowCount) {
        this.setRowCount(rowCount);
        return this;
    }

    public Skip withJdbcParameter(JdbcParameter jdbcParameter) {
        this.setJdbcParameter(jdbcParameter);
        return this;
    }

    public Skip withVariable(String variable) {
        this.setVariable(variable);
        return this;
    }
}
