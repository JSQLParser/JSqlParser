/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * A {@code CREATE DATABASE} statement. Modelled after MySQL, where the statement takes an optional
 * {@code IF NOT EXISTS} and vendor-specific options, which are captured verbatim.
 */
public class CreateDatabase implements Statement {

    private String databaseName;
    private boolean hasIfNotExists = false;
    private List<String> databaseOptions = null;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    /**
     * The name of the database.
     *
     * @return the database name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    public CreateDatabase setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public boolean hasIfNotExists() {
        return hasIfNotExists;
    }

    public CreateDatabase setIfNotExists(boolean hasIfNotExists) {
        this.hasIfNotExists = hasIfNotExists;
        return this;
    }

    /**
     * The vendor-specific options of the database, captured as raw tokens.
     *
     * @return the list of option tokens, {@code null} when no options were given
     */
    public List<String> getDatabaseOptions() {
        return databaseOptions;
    }

    public CreateDatabase setDatabaseOptions(List<String> databaseOptions) {
        this.databaseOptions = databaseOptions;
        return this;
    }

    public CreateDatabase withDatabaseName(String databaseName) {
        return this.setDatabaseName(databaseName);
    }

    public CreateDatabase withIfNotExists(boolean hasIfNotExists) {
        return this.setIfNotExists(hasIfNotExists);
    }

    public CreateDatabase withDatabaseOptions(List<String> databaseOptions) {
        return this.setDatabaseOptions(databaseOptions);
    }

    public CreateDatabase addDatabaseOptions(String... databaseOptions) {
        List<String> collection =
                Optional.ofNullable(getDatabaseOptions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, databaseOptions);
        return this.withDatabaseOptions(collection);
    }

    public CreateDatabase addDatabaseOptions(Collection<String> databaseOptions) {
        List<String> collection =
                Optional.ofNullable(getDatabaseOptions()).orElseGet(ArrayList::new);
        collection.addAll(databaseOptions);
        return this.withDatabaseOptions(collection);
    }

    @Override
    public String toString() {
        String sql = "CREATE DATABASE";
        if (hasIfNotExists) {
            sql += " IF NOT EXISTS";
        }
        if (databaseName != null) {
            sql += " " + databaseName;
        }
        if (databaseOptions != null && !databaseOptions.isEmpty()) {
            sql += " " + String.join(" ", databaseOptions);
        }
        return sql;
    }
}
