/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.cursor;

/**
 * An option of PostgreSQL's {@code DECLARE name [options] CURSOR}. PostgreSQL's grammar takes them
 * in any order and repeated; the server rejects contradicting ones such as SCROLL and NO SCROLL
 * when it runs the statement.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-declare.html">PostgreSQL DECLARE</a>
 */
public enum CursorOption {
    /** Returns the rows in binary rather than text format. */
    BINARY("BINARY"),

    /** Leaves the visibility of later changes to the implementation. */
    ASENSITIVE("ASENSITIVE"),

    /** Hides later changes; PostgreSQL's cursors are always insensitive. */
    INSENSITIVE("INSENSITIVE"),

    /** Allows fetching backward. */
    SCROLL("SCROLL"),

    /** Forbids fetching backward. */
    NO_SCROLL("NO SCROLL");

    private final String keyword;

    CursorOption(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
