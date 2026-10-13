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

/** PostgreSQL's keyword categories, also used by DuckDB. */
public enum KeywordCategory {
    /** Usable as any name. */
    UNRESERVED,
    /** Usable as a column, table or alias name, not as a function or type name. */
    COLUMN_NAME,
    /** Usable as a function or type name, not as a column, table or alias name. */
    TYPE_FUNCTION,
    /** Usable as a name only after {@code AS} or {@code .}. */
    RESERVED
}
