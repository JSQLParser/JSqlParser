/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

public enum SqlServerTableHintType {
    INDEX,
    FORCESEEK,
    FORCESCAN,
    SPATIAL_WINDOW_MAX_CELLS,
    HOLDLOCK,
    NOLOCK,
    NOWAIT,
    PAGLOCK,
    READCOMMITTED,
    READCOMMITTEDLOCK,
    READPAST,
    READUNCOMMITTED,
    REPEATABLEREAD,
    ROWLOCK,
    SERIALIZABLE,
    SNAPSHOT,
    TABLOCK,
    TABLOCKX,
    UPDLOCK,
    XLOCK,
    KEEPIDENTITY,
    KEEPDEFAULTS,
    IGNORE_CONSTRAINTS,
    IGNORE_TRIGGERS,
}
