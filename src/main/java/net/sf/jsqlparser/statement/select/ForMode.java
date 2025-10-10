/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

/**
 * @author jxnu-liguobin
 */
public enum ForMode {

    UPDATE("UPDATE"),

    SHARE("SHARE"),

    NO_KEY_UPDATE("NO KEY UPDATE"),

    KEY_SHARE("KEY SHARE"),

    // https://www.ibm.com/docs/en/db2-for-zos/13.0.0?topic=statement-read-only-clause
    READ_ONLY("READ ONLY"),
    FETCH_ONLY("FETCH ONLY");

    private final String value;

    ForMode(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
