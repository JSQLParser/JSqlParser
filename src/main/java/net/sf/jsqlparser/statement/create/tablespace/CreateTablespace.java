/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.tablespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.IndexOption;
import net.sf.jsqlparser.statement.create.table.TableOption;

/**
 * A MySQL {@code CREATE [UNDO] TABLESPACE} or PostgreSQL {@code CREATE TABLESPACE} statement. The
 * parser fills either the PostgreSQL owner, location and parameters, or the MySQL data file and
 * options. MySQL options reuse the {@link TableOption} models of the same name, plus
 * {@link TableOption.Kind#FILE_BLOCK_SIZE}.
 */
public class CreateTablespace implements Statement {
    private String name;
    private boolean undo;
    private String owner;
    private StringValue location;
    private List<IndexOption> parameters;
    private StringValue dataFile;
    private final List<TableOption> options = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Whether this is a MySQL {@code CREATE UNDO TABLESPACE} statement. */
    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    /** Returns the PostgreSQL {@code OWNER} role, or null when the clause is omitted. */
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /** Returns the PostgreSQL {@code LOCATION} directory. */
    public StringValue getLocation() {
        return location;
    }

    public void setLocation(StringValue location) {
        this.location = location;
    }

    /**
     * Returns the PostgreSQL {@code WITH (...)} tablespace parameters, or null when the clause is
     * omitted.
     */
    public List<IndexOption> getParameters() {
        return parameters;
    }

    public void setParameters(List<IndexOption> parameters) {
        this.parameters = parameters;
    }

    /** Returns the MySQL {@code ADD DATAFILE} file name, or null when the clause is omitted. */
    public StringValue getDataFile() {
        return dataFile;
    }

    public void setDataFile(StringValue dataFile) {
        this.dataFile = dataFile;
    }

    /** Returns the mutable MySQL options in source order; commas between them are not kept. */
    public List<TableOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<TableOption> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public CreateTablespace addOptions(TableOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    /** Returns the first MySQL option of the given kind. */
    public Optional<TableOption> getOption(TableOption.Kind kind) {
        return options.stream().filter(option -> option.getKind() == kind).findFirst();
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append(undo ? "CREATE UNDO TABLESPACE " : "CREATE TABLESPACE ").append(name);
        if (owner != null) {
            builder.append(" OWNER ").append(owner);
        }
        if (location != null) {
            builder.append(" LOCATION ").append(location);
        }
        if (parameters != null) {
            builder.append(" WITH ");
            IndexOption.appendListTo(builder, parameters, expressionPrinter);
        }
        if (dataFile != null) {
            builder.append(" ADD DATAFILE ").append(dataFile);
        }
        for (TableOption option : options) {
            builder.append(' ');
            option.appendTo(builder, expressionPrinter);
        }
        return builder;
    }

    /** Visits the values of the PostgreSQL tablespace parameters. */
    public void visitExpressions(Consumer<Expression> visitor) {
        if (parameters != null) {
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
