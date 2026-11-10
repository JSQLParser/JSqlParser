/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.resourcegroup;

/**
 * How a MySQL resource group option is joined to its value: {@code TYPE USER}, {@code TYPE = USER}
 * or {@code TYPE := USER}. MySQL reads the three alike.
 */
public enum OptionAssignment {
    NONE(""), EQUALS("="), COLON_EQUALS(":=");

    private final String symbol;

    OptionAssignment(String symbol) {
        this.symbol = symbol;
    }

    /** The symbol as written, empty for {@link #NONE}. */
    public String getSymbol() {
        return symbol;
    }

    /** Appends the option name, the symbol if there is one, and a space before the value. */
    StringBuilder appendTo(StringBuilder builder, String option) {
        builder.append(option).append(' ');
        return this == NONE ? builder : builder.append(symbol).append(' ');
    }
}
