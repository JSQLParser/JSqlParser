/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

/**
 * The table target of a SELECT: {@code INTO [persistence] [TABLE] table}.
 *
 * <p>
 * PostgreSQL creates the table with the persistence written before its name, such as
 * {@code INTO TEMP t}, {@code INTO LOCAL TEMPORARY TABLE t} or {@code INTO UNLOGGED t}. Other
 * products write the plain {@code INTO t} after the select list as well, and Informix writes
 * {@code INTO TEMP t} at the end of the query, which is the {@link Position#TRAILING} position.
 */
public class SelectIntoClause extends ASTNodeAccessImpl implements Serializable {
    /** Where the clause is written in the SELECT. */
    public enum Position {
        BEFORE_FROM, TRAILING
    }

    /** The persistence keywords PostgreSQL accepts before the table name. */
    public enum Persistence {
        TEMP, TEMPORARY, LOCAL_TEMP, LOCAL_TEMPORARY, GLOBAL_TEMP, GLOBAL_TEMPORARY, UNLOGGED;

        private final String keywords = name().replace('_', ' ');

        /** Reads the keywords as written, for example {@code "LOCAL TEMP"}. */
        public static Persistence from(String keywords) {
            return valueOf(keywords.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_"));
        }

        public String getKeywords() {
            return keywords;
        }

        public boolean isTemporary() {
            return this != UNLOGGED;
        }
    }

    private Position position = Position.BEFORE_FROM;
    private Persistence persistence;
    private boolean tableKeyword;
    private List<Table> tables = new ArrayList<>();

    public SelectIntoClause() {
        // empty constructor
    }

    public SelectIntoClause(Table table) {
        tables.add(Objects.requireNonNull(table, "table"));
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "position");
    }

    public SelectIntoClause withPosition(Position position) {
        setPosition(position);
        return this;
    }

    /** The persistence written before the table name, or null when there is none. */
    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public SelectIntoClause withPersistence(Persistence persistence) {
        setPersistence(persistence);
        return this;
    }

    /** Whether the optional TABLE keyword precedes the table name. */
    public boolean isTableKeyword() {
        return tableKeyword;
    }

    public void setTableKeyword(boolean tableKeyword) {
        this.tableKeyword = tableKeyword;
    }

    public SelectIntoClause withTableKeyword(boolean tableKeyword) {
        setTableKeyword(tableKeyword);
        return this;
    }

    /** The first target, which is the only one of a clause that creates a table. */
    public Table getTable() {
        return tables.isEmpty() ? null : tables.get(0);
    }

    /** The targets in the order written. The list is the clause's own and may be modified. */
    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = Objects.requireNonNull(tables, "tables");
    }

    public SelectIntoClause withTables(List<Table> tables) {
        setTables(tables);
        return this;
    }

    public SelectIntoClause addTable(Table table) {
        tables.add(Objects.requireNonNull(table, "table"));
        return this;
    }

    /** Appends the clause without a leading space, rendering each table through the renderer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> tableRenderer) {
        builder.append("INTO ");
        if (persistence != null) {
            builder.append(persistence.getKeywords()).append(' ');
        }
        if (tableKeyword) {
            builder.append("TABLE ");
        }
        for (int i = 0; i < tables.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            tableRenderer.accept(tables.get(i));
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
