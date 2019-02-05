/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2015 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.JdbcParameter;

/**
 * A skip clause in the form [SKIP row_count]
 *
 * Initial implementation was done for informix special syntax:
 * http://www-01.ibm.com/support/knowledgecenter/SSGU8G_12.1.0/com.ibm.sqls.doc/ids_sqs_0156.htm
 */
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
}
