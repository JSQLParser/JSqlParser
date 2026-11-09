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

/**
 * MySQL {@code [NO] RELEASE} after COMMIT or ROLLBACK; a null field leaves the server default
 * unchanged.
 */
public enum TransactionRelease {
    RELEASE("RELEASE"), NO_RELEASE("NO RELEASE");

    private final String sql;

    TransactionRelease(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return sql;
    }
}
