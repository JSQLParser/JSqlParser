/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;

public class DBMSType implements SourceDestinationType {
    private final Kind dbmsType;
    private StringValue jdbcDriverDefinition;

    public DBMSType(String dbmsType) {
        this(dbmsType, null);
    }

    public DBMSType(String dbmsType, String jdbcDriverDefinition) {
        this.dbmsType = Kind.valueOf(dbmsType.toUpperCase());
        if (jdbcDriverDefinition != null) {
            this.jdbcDriverDefinition = new StringValue(jdbcDriverDefinition);
        }
    }

    private enum Kind {
        EXA, ORA, JDBC
    }

    public StringValue getJDBCDriverDefinition() {
        return jdbcDriverDefinition;
    }

    public void setJDBCDriverDefinition(StringValue jdbcDriverDefinition) {
        this.jdbcDriverDefinition = jdbcDriverDefinition;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(dbmsType);
        if (jdbcDriverDefinition != null) {
            sql.append(" DRIVER = ").append(jdbcDriverDefinition);
        }

        return sql.toString();
    }
}
