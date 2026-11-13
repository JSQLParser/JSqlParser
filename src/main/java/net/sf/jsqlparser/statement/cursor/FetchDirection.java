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
 * The direction keyword of PostgreSQL's {@code FETCH} and {@code MOVE}. ABSOLUTE and RELATIVE take
 * a count, FORWARD and BACKWARD take an optional count or ALL, and the others take neither. Without
 * a keyword, a count or ALL alone fetches forward.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-fetch.html">PostgreSQL FETCH</a>
 */
public enum FetchDirection {
    NEXT, PRIOR, FIRST, LAST, ABSOLUTE, RELATIVE, FORWARD, BACKWARD;

    /** Whether a count must follow: ABSOLUTE n and RELATIVE n. */
    public boolean isCountRequired() {
        return this == ABSOLUTE || this == RELATIVE;
    }

    /** Whether a count or ALL may follow: FORWARD and BACKWARD. */
    public boolean isCountOptional() {
        return this == FORWARD || this == BACKWARD;
    }
}
