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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * A {@code CREATE DATABASE} statement with an optional {@code IF NOT EXISTS} and options. MySQL and
 * PostgreSQL options are typed {@link DatabaseOption}s; other option text is kept as a raw
 * {@link DatabaseOption.Kind#OTHER} option.
 */
public class CreateDatabase implements Statement {

    private String databaseName;
    private boolean hasIfNotExists = false;
    private boolean useWith;
    private final List<DatabaseOption> options = new ArrayList<>();

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
     * Whether PostgreSQL's optional {@code WITH} precedes the options.
     *
     * @return {@code true} if {@code WITH} is rendered
     */
    public boolean isUseWith() {
        return useWith;
    }

    public CreateDatabase setUseWith(boolean useWith) {
        this.useWith = useWith;
        return this;
    }

    /**
     * The options in their legacy flat-token form, including a leading {@code WITH}. This is an
     * unmodifiable snapshot of {@link #getOptions()}; edit the options or use
     * {@link #setDatabaseOptions(List)} instead.
     *
     * @return the option tokens, {@code null} when no options were given
     */
    public List<String> getDatabaseOptions() {
        if (options.isEmpty() && !useWith) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        if (useWith) {
            tokens.add("WITH");
        }
        options.forEach(option -> tokens.addAll(option.getTokens()));
        return Collections.unmodifiableList(tokens);
    }

    /**
     * Replaces all options, including {@code WITH}, with one raw option of these tokens. The tokens
     * are not parsed.
     *
     * @param databaseOptions the option tokens, or {@code null} to remove all options
     * @return this statement
     */
    public CreateDatabase setDatabaseOptions(List<String> databaseOptions) {
        options.clear();
        useWith = false;
        if (databaseOptions != null && !databaseOptions.isEmpty()) {
            options.add(DatabaseOption.raw(databaseOptions));
        }
        return this;
    }

    /**
     * The options in source order, as a mutable list.
     *
     * @return the options, empty if there are none
     */
    public List<DatabaseOption> getOptions() {
        return options;
    }

    /**
     * Replaces the options with a copy of the given list.
     *
     * @param options the options, or {@code null} to remove all options
     * @return this statement
     */
    public CreateDatabase setOptions(List<DatabaseOption> options) {
        List<DatabaseOption> copy =
                options == null ? Collections.emptyList() : new ArrayList<>(options);
        this.options.clear();
        this.options.addAll(copy);
        return this;
    }

    public Optional<DatabaseOption> getOption(DatabaseOption.Kind kind) {
        return options.stream().filter(option -> option.getKind() == kind).findFirst();
    }

    public CreateDatabase withDatabaseName(String databaseName) {
        return this.setDatabaseName(databaseName);
    }

    public CreateDatabase withIfNotExists(boolean hasIfNotExists) {
        return this.setIfNotExists(hasIfNotExists);
    }

    public CreateDatabase withUseWith(boolean useWith) {
        return this.setUseWith(useWith);
    }

    public CreateDatabase withDatabaseOptions(List<String> databaseOptions) {
        return this.setDatabaseOptions(databaseOptions);
    }

    public CreateDatabase withOptions(List<DatabaseOption> options) {
        return setOptions(options);
    }

    public CreateDatabase addOptions(DatabaseOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    /**
     * Appends one raw option of these tokens, keeping the existing options.
     *
     * @param databaseOptions the option tokens
     * @return this statement
     */
    public CreateDatabase addDatabaseOptions(String... databaseOptions) {
        return addDatabaseOptions(Arrays.asList(databaseOptions));
    }

    /**
     * Appends one raw option of these tokens, keeping the existing options.
     *
     * @param databaseOptions the option tokens
     * @return this statement
     */
    public CreateDatabase addDatabaseOptions(Collection<String> databaseOptions) {
        if (!databaseOptions.isEmpty()) {
            options.add(DatabaseOption.raw(new ArrayList<>(databaseOptions)));
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("CREATE DATABASE");
        if (hasIfNotExists) {
            sql.append(" IF NOT EXISTS");
        }
        if (databaseName != null) {
            sql.append(' ').append(databaseName);
        }
        if (useWith) {
            sql.append(" WITH");
        }
        for (DatabaseOption option : options) {
            String text = option.toString();
            if (!text.isEmpty()) {
                sql.append(' ').append(text);
            }
        }
        return sql.toString();
    }
}
