/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.refreshView;

public enum RefreshMode {

    DEFAULT, WITH_DATA, WITH_NO_DATA;

    public static RefreshMode from(String type) {
        return Enum.valueOf(RefreshMode.class, type.toUpperCase());
    }
}
