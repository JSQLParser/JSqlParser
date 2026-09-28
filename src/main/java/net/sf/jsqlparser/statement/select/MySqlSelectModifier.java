/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

/**
 * MySQL SELECT modifiers, in rendering order. SQL_CACHE is retained for older MySQL releases and
 * MariaDB. DISTINCT and ALL remain part of the common SELECT grammar.
 */
public enum MySqlSelectModifier {
    HIGH_PRIORITY, STRAIGHT_JOIN, SQL_SMALL_RESULT, SQL_BIG_RESULT, SQL_BUFFER_RESULT, SQL_CACHE, SQL_NO_CACHE, SQL_CALC_FOUND_ROWS
}
