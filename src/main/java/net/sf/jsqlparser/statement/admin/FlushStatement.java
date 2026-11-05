/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.TableMaintenanceStatement;

/**
 * MySQL {@code FLUSH}, either with a list of {@link FlushOption flush options} or as
 * {@code FLUSH TABLES}, which may name tables and lock them.<br>
 * Examples:<br>
 * FLUSH LOCAL BINARY LOGS, RELAY LOGS FOR CHANNEL 'c1'<br>
 * FLUSH TABLES t, u WITH READ LOCK<br>
 * <br>
 * Without options the statement is {@code FLUSH TABLES}: {@link #isFlushTables()} derives the form
 * from {@link #getOptions()}, and the tables, the spelling and the lock apply only to that form.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/flush.html">MySQL FLUSH</a>
 */
public class FlushStatement extends ASTNodeAccessImpl implements Statement {
    private TableMaintenanceStatement.Modifier modifier;
    private final List<FlushOption> options = new ArrayList<>();
    private boolean tablesKeyword = true;
    private final List<Table> tables = new ArrayList<>();
    private TableLock tableLock;

    /** What {@code FLUSH TABLES} does to the tables after flushing them. */
    public enum TableLock {
        /** {@code WITH READ LOCK}: the named tables, or every table, stay read locked. */
        WITH_READ_LOCK("WITH READ LOCK"),
        /** {@code FOR EXPORT}: the named tables are made ready to copy their files. */
        FOR_EXPORT("FOR EXPORT");

        private final String keyword;

        TableLock(String keyword) {
            this.keyword = keyword;
        }

        public String getKeyword() {
            return keyword;
        }
    }

    /** Returns {@code NO_WRITE_TO_BINLOG} or its synonym {@code LOCAL}, or null. */
    public TableMaintenanceStatement.Modifier getModifier() {
        return modifier;
    }

    public void setModifier(TableMaintenanceStatement.Modifier modifier) {
        this.modifier = modifier;
    }

    public FlushStatement withModifier(TableMaintenanceStatement.Modifier modifier) {
        setModifier(modifier);
        return this;
    }

    /** Returns the flush options in source order; empty for {@code FLUSH TABLES}. */
    public List<FlushOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setOptions(Collection<FlushOption> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public FlushStatement withOptions(Collection<FlushOption> options) {
        setOptions(options);
        return this;
    }

    public FlushStatement addOptions(FlushOption... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    /** Whether the statement is {@code FLUSH TABLE} or {@code FLUSH TABLES}: it has no options. */
    public boolean isFlushTables() {
        return options.isEmpty();
    }

    /** Whether {@code FLUSH TABLES} is spelled with {@code TABLES} instead of {@code TABLE}. */
    public boolean isTablesKeyword() {
        return tablesKeyword;
    }

    public void setTablesKeyword(boolean tablesKeyword) {
        this.tablesKeyword = tablesKeyword;
    }

    public FlushStatement withTablesKeyword(boolean tablesKeyword) {
        setTablesKeyword(tablesKeyword);
        return this;
    }

    /** Returns the tables of {@code FLUSH TABLES}; empty when it flushes every table. */
    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public void setTables(Collection<? extends Table> tables) {
        this.tables.clear();
        if (tables != null) {
            this.tables.addAll(tables);
        }
    }

    public FlushStatement withTables(Collection<? extends Table> tables) {
        setTables(tables);
        return this;
    }

    public FlushStatement addTables(Table... tables) {
        Collections.addAll(this.tables, tables);
        return this;
    }

    /** Returns the lock of {@code FLUSH TABLES}, or null. */
    public TableLock getTableLock() {
        return tableLock;
    }

    public void setTableLock(TableLock tableLock) {
        this.tableLock = tableLock;
    }

    public FlushStatement withTableLock(TableLock tableLock) {
        setTableLock(tableLock);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("FLUSH");
        if (modifier != null) {
            builder.append(' ').append(modifier);
        }
        if (!options.isEmpty()) {
            for (int i = 0; i < options.size(); i++) {
                builder.append(i > 0 ? ", " : " ");
                options.get(i).appendTo(builder);
            }
            return builder;
        }
        builder.append(tablesKeyword ? " TABLES" : " TABLE");
        for (int i = 0; i < tables.size(); i++) {
            builder.append(i > 0 ? ", " : " ").append(tables.get(i).getFullyQualifiedName());
        }
        if (tableLock != null) {
            builder.append(' ').append(tableLock.getKeyword());
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
