/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * A MySQL {@code ALTER [UNDO] TABLESPACE} or PostgreSQL {@code ALTER TABLESPACE} statement. The
 * {@link Action} selects which properties are rendered. Moving all relations of a tablespace with
 * {@code ALTER TABLE ALL IN TABLESPACE} remains {@link AlterTablespaceMove}.
 */
public class AlterTablespace implements Statement {

    public enum Action {
        /** {@code RENAME TO newName}. */
        RENAME,
        /** PostgreSQL {@code OWNER TO owner}. */
        OWNER,
        /** PostgreSQL {@code SET (parameter = value, ...)}. */
        SET_PARAMETERS,
        /** PostgreSQL {@code RESET (parameter, ...)}. */
        RESET_PARAMETERS,
        /** MySQL undo tablespace {@code SET ACTIVE} or {@code SET INACTIVE}, then any options. */
        SET_STATE,
        /** MySQL options such as {@code ENGINE} or {@code ENCRYPTION}. */
        OPTIONS
    }

    /** The state of a MySQL undo tablespace. */
    public enum State {
        ACTIVE, INACTIVE
    }

    private String name;
    private boolean undo;
    private Action action;
    private String newName;
    private String owner;
    private List<IndexOption> parameters;
    private List<String> resetParameters;
    private State state;
    private final List<TableOption> options = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Whether this is a MySQL {@code ALTER UNDO TABLESPACE} statement. */
    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /** Returns the new name of a {@link Action#RENAME}. */
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    /** Returns the new owner of an {@link Action#OWNER}. */
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /** Returns the PostgreSQL parameters of a {@link Action#SET_PARAMETERS}. */
    public List<IndexOption> getParameters() {
        return parameters;
    }

    public void setParameters(List<IndexOption> parameters) {
        this.parameters = parameters;
    }

    /** Returns the PostgreSQL parameter names of a {@link Action#RESET_PARAMETERS}. */
    public List<String> getResetParameters() {
        return resetParameters;
    }

    public void setResetParameters(List<String> resetParameters) {
        this.resetParameters = resetParameters;
    }

    /** Returns the MySQL undo tablespace state of a {@link Action#SET_STATE}. */
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /**
     * Returns the mutable MySQL options of a {@link Action#SET_STATE} or {@link Action#OPTIONS} in
     * source order; commas between them are not kept.
     */
    public List<TableOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<TableOption> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public AlterTablespace addOptions(TableOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    /** Returns the first MySQL option of the given kind. */
    public Optional<TableOption> getOption(TableOption.Kind kind) {
        return options.stream().filter(option -> option.getKind() == kind).findFirst();
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        if (action == null) {
            throw new IllegalStateException("ALTER TABLESPACE requires an action");
        }
        builder.append(undo ? "ALTER UNDO TABLESPACE " : "ALTER TABLESPACE ").append(name);
        switch (action) {
            case RENAME:
                builder.append(" RENAME TO ").append(newName);
                break;
            case OWNER:
                builder.append(" OWNER TO ").append(owner);
                break;
            case SET_PARAMETERS:
                builder.append(" SET ");
                IndexOption.appendListTo(builder, parameters, expressionPrinter);
                break;
            case RESET_PARAMETERS:
                builder.append(" RESET ")
                        .append(PlainSelect.getStringList(resetParameters, true, true));
                break;
            case SET_STATE:
                builder.append(" SET ").append(state);
                appendOptionsTo(builder, expressionPrinter);
                break;
            case OPTIONS:
                appendOptionsTo(builder, expressionPrinter);
                break;
            default:
                throw new IllegalStateException("Unknown tablespace action: " + action);
        }
        return builder;
    }

    private void appendOptionsTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        for (TableOption option : options) {
            builder.append(' ');
            option.appendTo(builder, expressionPrinter);
        }
    }

    /** Visits the values of PostgreSQL {@code SET} parameters. */
    public void visitExpressions(Consumer<Expression> visitor) {
        if (action == Action.SET_PARAMETERS && parameters != null) {
            parameters.stream().map(IndexOption::getValue).filter(Objects::nonNull)
                    .forEach(visitor);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
