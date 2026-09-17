/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

/**
 * DuckDB's {@code ATTACH [DATABASE] [IF NOT EXISTS] path [AS alias] [(options)]}, which adds
 * another database file or remote database to the session.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/attach">ATTACH</a>
 */
public class AttachStatement implements Statement {
    private String databasePath;
    private String alias;
    private boolean usingDatabaseKeyword;
    private boolean ifNotExists;
    private List<String> options;

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public AttachStatement withDatabasePath(String databasePath) {
        setDatabasePath(databasePath);
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public AttachStatement withAlias(String alias) {
        setAlias(alias);
        return this;
    }

    public boolean isUsingDatabaseKeyword() {
        return usingDatabaseKeyword;
    }

    public void setUsingDatabaseKeyword(boolean usingDatabaseKeyword) {
        this.usingDatabaseKeyword = usingDatabaseKeyword;
    }

    public AttachStatement withUsingDatabaseKeyword(boolean usingDatabaseKeyword) {
        setUsingDatabaseKeyword(usingDatabaseKeyword);
        return this;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public AttachStatement withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
        return this;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public AttachStatement withOptions(List<String> options) {
        setOptions(options);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ATTACH ");
        if (usingDatabaseKeyword) {
            builder.append("DATABASE ");
        }
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(databasePath);
        if (alias != null) {
            builder.append(" AS ").append(alias);
        }
        if (options != null && !options.isEmpty()) {
            builder.append(" ").append(PlainSelect.getStringList(options, true, true));
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
