/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.dialect;

/**
 * A dialect's own reserved-keyword rules, replacing the default ones: which words it accepts as
 * unquoted names in each position.
 */
public interface KeywordPolicy {
    /** Whether {@code word}, a keyword or not, is a valid unquoted name at {@code position}. */
    boolean isName(String word, NamePosition position);

    /** Where a name appears, since dialects reserve keywords differently in each. */
    enum NamePosition {
        /** A column, table, schema, CTE or other object name. */
        NAME,
        /** A function name, directly followed by {@code (}. */
        FUNCTION,
        /** A name after {@code AS} or {@code .}. */
        LABEL,
        /** An alias without {@code AS}. */
        BARE_ALIAS
    }
}
