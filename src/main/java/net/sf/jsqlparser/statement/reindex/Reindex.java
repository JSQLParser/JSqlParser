/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.reindex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** A PostgreSQL {@code REINDEX} statement. */
public class Reindex implements Statement {

    public enum Target {
        INDEX, TABLE, SCHEMA, DATABASE, SYSTEM
    }

    private final List<Option> options = new ArrayList<>();
    private Target target;
    private boolean concurrently;
    private Table name;

    /** A parenthesized option, for example {@code VERBOSE} or {@code TABLESPACE fast_space}. */
    public static class Option implements Serializable {
        public enum Name {
            CONCURRENTLY, TABLESPACE, VERBOSE
        }

        private Name name;
        private String value;

        public Option() {
            // empty constructor
        }

        public Option(Name name, String value) {
            this.name = name;
            this.value = value;
        }

        public Name getName() {
            return name;
        }

        public void setName(Name name) {
            this.name = name;
        }

        /**
         * The value as written, such as {@code false}, {@code 0} or a tablespace; null if omitted.
         */
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value == null ? name.name() : name + " " + value;
        }
    }

    public Reindex() {
        // empty constructor
    }

    public Reindex(Target target, Table name) {
        this.target = target;
        this.name = name;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    /** The parenthesized options in source order; empty when the list is omitted. */
    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setOptions(Collection<Option> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public Reindex addOptions(Option... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    /** Whether the {@code CONCURRENTLY} keyword follows the target kind. */
    public boolean isConcurrently() {
        return concurrently;
    }

    public void setConcurrently(boolean concurrently) {
        this.concurrently = concurrently;
    }

    /**
     * The index, table, schema or database name. Only DATABASE and SYSTEM may omit it, which
     * selects the current database.
     */
    public Table getName() {
        return name;
    }

    public void setName(Table name) {
        this.name = name;
    }

    /** Returns the reindexed table, or null when the target is not a table. */
    public Table getTable() {
        return target == Target.TABLE ? name : null;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("REINDEX ");
        if (!options.isEmpty()) {
            sql.append(options.stream().map(Option::toString)
                    .collect(Collectors.joining(", ", "(", ") ")));
        }
        sql.append(target);
        if (concurrently) {
            sql.append(" CONCURRENTLY");
        }
        if (name != null) {
            sql.append(' ').append(name);
        }
        return sql;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public Reindex withTarget(Target target) {
        setTarget(target);
        return this;
    }

    public Reindex withConcurrently(boolean concurrently) {
        setConcurrently(concurrently);
        return this;
    }

    public Reindex withName(Table name) {
        setName(name);
        return this;
    }

    public Reindex withOptions(Collection<Option> options) {
        setOptions(options);
        return this;
    }
}
