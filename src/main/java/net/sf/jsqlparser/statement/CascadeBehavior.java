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
 * An explicit CASCADE or RESTRICT clause. A null field leaves the clause omitted and preserves the
 * database's default behavior for that statement.
 */
public enum CascadeBehavior {
    CASCADE, RESTRICT
}
