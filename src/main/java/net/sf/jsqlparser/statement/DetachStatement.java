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

/**
 * DuckDB's {@code DETACH [DATABASE] [IF EXISTS] name}, the counterpart of {@link AttachStatement}.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/attach">DETACH</a>
 */
public class DetachStatement implements Statement {
    private String databaseName;
    private boolean usingDatabaseKeyword;
    private boolean ifExists;

    public DetachStatement() {}

    public DetachStatement(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public DetachStatement withDatabaseName(String databaseName) {
        setDatabaseName(databaseName);
        return this;
    }

    public boolean isUsingDatabaseKeyword() {
        return usingDatabaseKeyword;
    }

    public void setUsingDatabaseKeyword(boolean usingDatabaseKeyword) {
        this.usingDatabaseKeyword = usingDatabaseKeyword;
    }

    public DetachStatement withUsingDatabaseKeyword(boolean usingDatabaseKeyword) {
        setUsingDatabaseKeyword(usingDatabaseKeyword);
        return this;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public DetachStatement withIfExists(boolean ifExists) {
        setIfExists(ifExists);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("DETACH ");
        if (usingDatabaseKeyword) {
            builder.append("DATABASE ");
        }
        if (ifExists) {
            builder.append("IF EXISTS ");
        }
        builder.append(databaseName);
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
