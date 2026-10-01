/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

/** Shared operations for ALTER SERVER and ALTER FOREIGN DATA WRAPPER. */
public enum ForeignObjectAlterAction {
    /** Changes options and the object's server version or wrapper functions, when supplied. */
    OPTIONS, OWNER, RENAME
}
