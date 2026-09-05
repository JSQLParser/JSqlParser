/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.event;

public enum EventStatus {
    ENABLE("ENABLE"), DISABLE("DISABLE"), DISABLE_ON_SLAVE("DISABLE ON SLAVE");

    private final String sql;

    EventStatus(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
