/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

/**
 * Describes the string used to separate the key from the value.
 */
public enum JsonKeyValuePairSeparator {
    VALUE(" VALUE "), COLON(":"),

    // Used in MySQL dialect
    COMMA(","),

    // Is used in case they KeyValuePair has only a key and no value
    NOT_USED("");

    private final String separator;

    JsonKeyValuePairSeparator(String separator) {
        this.separator = separator;
    }

    public String getSeparatorString() {
        return separator;
    }
}
