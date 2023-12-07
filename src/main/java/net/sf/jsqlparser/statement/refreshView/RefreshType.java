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

public enum RefreshType {
    
    DEFAULT, WITH_DATA, WITH_NO_DATA;

    public static RefreshType from(String type) {
        return Enum.valueOf(RefreshType.class, type.toUpperCase());
    }
}
