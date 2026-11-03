/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

/**
 * The lock type that MySQL and MariaDB give each table of {@code LOCK TABLES}. MySQL 8.4 has
 * {@code READ [LOCAL] | WRITE}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/lock-tables.html">MySQL LOCK TABLES</a>
 * @see <a href="https://mariadb.com/kb/en/lock-tables/">MariaDB LOCK TABLES</a>
 */
public enum TableLockType {
    READ("READ"), READ_LOCAL("READ LOCAL"), WRITE("WRITE"),

    /**
     * MySQL 8.0, where it is deprecated and has no effect, and MariaDB. MySQL 8.4 rejects it.
     *
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/lock-tables.html">MySQL 8.0 LOCK
     *      TABLES</a>
     */
    LOW_PRIORITY_WRITE("LOW_PRIORITY WRITE"),

    /** MariaDB only. */
    WRITE_CONCURRENT("WRITE CONCURRENT");

    private final String value;

    TableLockType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
