/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.policy;

import java.util.Locale;

/**
 * PostgreSQL policy evaluation mode.
 */
public enum PolicyMode {
    PERMISSIVE, RESTRICTIVE;

    public static PolicyMode from(String mode) {
        return Enum.valueOf(PolicyMode.class, mode.toUpperCase(Locale.ROOT));
    }
}
