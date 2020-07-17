/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.feature;

public enum Feature {
    /**
     * SQL "UPSERT" statement is allowed
     */
    upsert,
    /**
     * SQL "MERGE" statement is allowed
     */
    merge,
    /**
     * SQL "INSERT" statement is allowed
     */
    insert,
    /**
     * insert with multi-value is allowed
     */
    insertWithMulivalue,
    /**
     * allows old oracle join syntax (+)
     */
    joinOldOracleSyntax,
    /**
     * allows oracle prior position
     */
    oraclePriorPosition,
    /**
     * allows square brackets for names, default is <code>false</code>
     */
    allowSquareBracketQuotation(false);

    private boolean enabled;

    /**
     * a feature which is enabled by default
     */
    private Feature() {
        this(true);
    }

    /**
     * Use this constructor to disable a feature by default.
     *
     * @param enabledByDefault
     */
    private Feature(boolean enabledByDefault) {
        this.enabled = enabledByDefault;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
