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
 * Commands to which a PostgreSQL policy can apply.
 */
public enum PolicyCommand {
    ALL, SELECT, INSERT, UPDATE, DELETE;

    public static PolicyCommand from(String command) {
        return Enum.valueOf(PolicyCommand.class, command.toUpperCase(Locale.ROOT));
    }
}
