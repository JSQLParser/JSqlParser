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
    select,
    /**
     * insert with multi-value is allowed
     */
    insertWithMulivalue,
    /**
     * SQL "INSERT" statement is allowed
     */
    insert,
    /**
     * SQL "UPDATE" statement is allowed
     */
    update,
    /**
     * SQL "DELETE" statement is allowed
     */
    delete,
    /**
     * SQL "UPSERT" statement is allowed
     */
    upsert,
    /**
     * SQL "MERGE" statement is allowed
     */
    merge,
    /**
     * SQL "ALTER" statement is allowed
     */
    alter,
    /**
     * SQL "TRUNCATE" statement is allowed
     */
    truncate,
    /**
     * SQL "EXECUTE" statement is allowed
     */
    execute,

    /**
     * allows old oracle join syntax (+)
     */
    joinOldOracleSyntax,
    /**
     * allows oracle prior position
     */
    oraclePriorPosition,
    /**
     * allows square brackets for names, disabled by default
     */
    allowSquareBracketQuotation(false);

    private boolean parserEnabled;
    private boolean switchable;

    /**
     * a feature which can't be enabled or disabled within the parser
     */
    private Feature() {
        this.parserEnabled = true;
        this.switchable = false;
    }

    /**
     * a feature which can be enabled or disabled by {@link FeatureConfiguration}
     *
     * @param parserEnabled
     */
    private Feature(boolean parserEnabled) {
        this.parserEnabled = parserEnabled;
        this.switchable = true;
    }

    public boolean isParserEnabled() {
        return parserEnabled;
    }

    public boolean isSwitchable() {
        return switchable;
    }

}
