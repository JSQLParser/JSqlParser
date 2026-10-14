/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.ConfigurationParameterChange;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.database.DatabaseOption;

/**
 * MySQL or PostgreSQL {@code ALTER DATABASE}. MySQL's {@code ALTER SCHEMA} synonym only changes
 * options, and a missing name selects the current database. The options use the same
 * {@link DatabaseOption} model as {@code CREATE DATABASE}. Names retain SQL identifier quoting.
 */
public class AlterDatabase implements Statement {

    public enum Action {
        /** MySQL options or PostgreSQL {@code [WITH] option ...}, see {@link #getOptions()}. */
        OPTIONS,
        /** {@code RENAME TO}, see {@link #getNewName()}. */
        RENAME,
        /** {@code OWNER TO}, see {@link #getOwner()}. */
        OWNER,
        /** {@code SET TABLESPACE}, see {@link #getTablespace()}. */
        SET_TABLESPACE,
        /** {@code REFRESH COLLATION VERSION}. */
        REFRESH_COLLATION_VERSION,
        /** {@code SET} or {@code RESET}, see {@link #getParameterChange()}. */
        CONFIGURATION_PARAMETER
    }

    private String databaseName;
    private boolean useSchemaKeyword;
    private Action action = Action.OPTIONS;
    private boolean useWith;
    private final List<DatabaseOption> options = new ArrayList<>();
    private String newName;
    private String owner;
    private String tablespace;
    private ConfigurationParameterChange parameterChange;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean isUseSchemaKeyword() {
        return useSchemaKeyword;
    }

    public void setUseSchemaKeyword(boolean useSchemaKeyword) {
        this.useSchemaKeyword = useSchemaKeyword;
    }

    /**
     * The rendered alteration. Payloads of other actions are retained for a later switch back.
     *
     * @return the action, {@link Action#OPTIONS} unless changed
     */
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Whether PostgreSQL's optional {@code WITH} precedes the options.
     *
     * @return {@code true} if {@code WITH} is rendered
     */
    public boolean isUseWith() {
        return useWith;
    }

    public void setUseWith(boolean useWith) {
        this.useWith = useWith;
    }

    /**
     * The options of {@link Action#OPTIONS}, as a mutable list.
     *
     * @return the options in source order
     */
    public List<DatabaseOption> getOptions() {
        return options;
    }

    public Optional<DatabaseOption> getOption(DatabaseOption.Kind kind) {
        return options.stream().filter(option -> option.getKind() == kind).findFirst();
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    /**
     * The new owner, a role name or {@code CURRENT_USER}, {@code CURRENT_ROLE} or
     * {@code SESSION_USER}.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public ConfigurationParameterChange getParameterChange() {
        return parameterChange;
    }

    public void setParameterChange(ConfigurationParameterChange parameterChange) {
        this.parameterChange = parameterChange;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    /**
     * Appends this statement to a builder.
     *
     * @param builder the target
     * @param valueRenderer appends each configuration parameter value
     * @return the builder
     */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> valueRenderer) {
        builder.append(useSchemaKeyword ? "ALTER SCHEMA" : "ALTER DATABASE");
        if (databaseName != null) {
            builder.append(' ').append(databaseName);
        }
        switch (action) {
            case RENAME:
                return builder.append(" RENAME TO ").append(newName);
            case OWNER:
                return builder.append(" OWNER TO ").append(owner);
            case SET_TABLESPACE:
                return builder.append(" SET TABLESPACE ").append(tablespace);
            case REFRESH_COLLATION_VERSION:
                return builder.append(" REFRESH COLLATION VERSION");
            case CONFIGURATION_PARAMETER:
                if (parameterChange == null) {
                    throw new IllegalStateException("Expected a configuration parameter change");
                }
                return parameterChange.appendTo(builder.append(' '), valueRenderer);
            default:
                if (useWith) {
                    builder.append(" WITH");
                }
                options.forEach(option -> builder.append(' ').append(option));
                return builder;
        }
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
