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

    // STATEMENT FEATURES
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
     * SQL "REPLACE" statement is allowed
     */
    replace,
    /**
     * SQL "DROP" statement is allowed
     */
    drop,
    /**
     * SQL "ALTER VIEW" statement is allowed
     */
    alterView,
    /**
     * SQL "CREATE VIEW" statement is allowed
     */
    createView,
    /**
     * SQL "CREATE TABLE" statement is allowed
     */
    createTable,
    /**
     * SQL "CREATE INDEX" statement is allowed
     */
    createIndex,
    /**
     * SQL "COMMIT" statement is allowed
     */
    commit,
    /**
     * SQL block starting with "BEGIN" and ends with "END" statement is allowed
     */
    block,
    /**
     * SQL "COMMENT" statement is allowed
     */
    comment,
    /**
     * SQL "DESCRIBE" statement is allowed
     */
    describe,
    /**
     * SQL "EXPLAIN" statement is allowed
     */
    explain,
    /**
     * SQL "CREATE SCHEMA" statement is allowed
     */
    createSchema,
    function,
    procedure,
    functionalStatement,

    // SYNTAX FEATURES
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
