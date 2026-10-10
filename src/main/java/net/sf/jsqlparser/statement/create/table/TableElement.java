/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.io.Serializable;

/**
 * A column or table constraint/index declared inside a {@code CREATE TABLE} definition.
 *
 * <p>
 * This common type lets callers inspect columns, constraints and indexes in their source order
 * without merging the separate typed lists themselves.
 */
public interface TableElement extends Serializable {
}
