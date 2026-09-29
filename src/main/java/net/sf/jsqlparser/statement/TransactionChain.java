/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

/** Explicit transaction chaining; a null field leaves the server default unchanged. */
public enum TransactionChain {
    CHAIN("AND CHAIN"), NO_CHAIN("AND NO CHAIN");

    private final String sql;

    TransactionChain(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
