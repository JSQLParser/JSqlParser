/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.view;

public enum AutoRefreshOption {
    NONE, YES, NO;

    public static AutoRefreshOption from(String option) {
        return Enum.valueOf(AutoRefreshOption.class, option.toUpperCase());
    }
}
