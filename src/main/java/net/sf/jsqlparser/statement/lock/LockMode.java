/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

/**
 * Describes the LockMode of a LOCK TABLE-Statement.
 */
public enum LockMode {
    // These two modes are more common
    Share("SHARE"), Exclusive("EXCLUSIVE"),

    // These are Oracle specific, as far as I know
    RowShare("ROW SHARE"), RowExclusive("ROW EXCLUSIVE"), ShareUpdate(
            "SHARE UPDATE"), ShareRowExclusive("SHARE ROW EXCLUSIVE");

    private final String value;

    LockMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
