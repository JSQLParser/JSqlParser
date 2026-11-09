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

/** A transaction characteristic of BEGIN, START TRANSACTION or SET TRANSACTION. */
public enum TransactionMode {
    ISOLATION_LEVEL_SERIALIZABLE("ISOLATION LEVEL SERIALIZABLE"), ISOLATION_LEVEL_REPEATABLE_READ(
            "ISOLATION LEVEL REPEATABLE READ"), ISOLATION_LEVEL_READ_COMMITTED(
                    "ISOLATION LEVEL READ COMMITTED"), ISOLATION_LEVEL_READ_UNCOMMITTED(
                            "ISOLATION LEVEL READ UNCOMMITTED"), READ_WRITE(
                                    "READ WRITE"), READ_ONLY("READ ONLY"), DEFERRABLE(
                                            "DEFERRABLE"), NOT_DEFERRABLE(
                                                    "NOT DEFERRABLE"), WITH_CONSISTENT_SNAPSHOT(
                                                            "WITH CONSISTENT SNAPSHOT");

    /**
     * The statement that lists the modes. The dialects accept different modes in each: MySQL BEGIN
     * accepts none, MySQL START TRANSACTION no isolation level, and only START TRANSACTION accepts
     * WITH CONSISTENT SNAPSHOT.
     */
    public enum Context {
        BEGIN, START_TRANSACTION, SET_TRANSACTION
    }

    private final String sql;

    TransactionMode(String sql) {
        this.sql = sql;
    }

    /** Whether this mode selects an isolation level. */
    public boolean isIsolationLevel() {
        return sql.startsWith("ISOLATION LEVEL");
    }

    /** Whether this mode is the READ ONLY or READ WRITE access mode. */
    public boolean isAccessMode() {
        return this == READ_ONLY || this == READ_WRITE;
    }

    /** Whether this mode is DEFERRABLE or NOT DEFERRABLE. */
    public boolean isDeferrability() {
        return this == DEFERRABLE || this == NOT_DEFERRABLE;
    }

    @Override
    public String toString() {
        return sql;
    }
}
