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

import java.util.Locale;

/**
 * The scope keyword after SET, shared by {@link SetStatement} assignments and
 * {@link SetTransactionStatement}.
 */
public enum SetScope {
    /** No scope keyword. */
    NONE,
    /** MySQL: the server-wide default. */
    GLOBAL,
    /** The current session. */
    SESSION,
    /** PostgreSQL: the current transaction; MySQL: a synonym of SESSION. */
    LOCAL,
    /** MySQL: GLOBAL, also written to the persisted configuration. */
    PERSIST,
    /** MySQL: only the persisted configuration. */
    PERSIST_ONLY;

    /**
     * Resolves a scope keyword, ignoring case.
     *
     * @param keyword the keyword, or null or empty for {@link #NONE}
     * @return the scope
     * @throws IllegalArgumentException if the keyword names no scope
     */
    public static SetScope fromKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return NONE;
        }
        return valueOf(keyword.toUpperCase(Locale.ROOT));
    }

    /** The keyword as written after SET, or null for {@link #NONE}. */
    public String getKeyword() {
        return this == NONE ? null : name();
    }
}
