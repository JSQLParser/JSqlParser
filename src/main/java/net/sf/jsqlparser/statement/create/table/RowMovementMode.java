/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

public enum RowMovementMode {
    ENABLE, DISABLE;

    public static RowMovementMode from(String mode) {
        return Enum.valueOf(RowMovementMode.class, mode.toUpperCase());
    }
}

