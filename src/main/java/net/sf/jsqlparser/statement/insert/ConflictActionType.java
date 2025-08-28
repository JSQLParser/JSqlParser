/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

public enum ConflictActionType {
    NOTHING, DO_NOTHING, DO_UPDATE;

    public static ConflictActionType from(String type) {
        return Enum.valueOf(ConflictActionType.class, type.toUpperCase());
    }
}
