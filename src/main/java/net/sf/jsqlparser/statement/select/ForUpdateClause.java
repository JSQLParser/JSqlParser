/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.List;
import net.sf.jsqlparser.schema.Table;

/**
 * Represents a FOR UPDATE / FOR SHARE locking clause in a SELECT statement.
 *
 * <p>
 * Supports all common SQL dialects:
 * <ul>
 * <li>{@code FOR UPDATE} – standard row locking</li>
 * <li>{@code FOR UPDATE OF t1, t2} – table-specific locking (Oracle, PostgreSQL)</li>
 * <li>{@code FOR UPDATE NOWAIT} – fail immediately if rows are locked (Oracle, PostgreSQL)</li>
 * <li>{@code FOR UPDATE WAIT n} – wait up to n seconds (Oracle)</li>
 * <li>{@code FOR UPDATE SKIP LOCKED} – skip locked rows (Oracle, PostgreSQL)</li>
 * <li>{@code FOR SHARE} – shared row lock (PostgreSQL)</li>
 * <li>{@code FOR KEY SHARE} – key-level shared lock (PostgreSQL)</li>
 * <li>{@code FOR NO KEY UPDATE} – non-key exclusive lock (PostgreSQL)</li>
 * </ul>
 * </p>
 */
public class ForUpdateClause {

    private ForMode mode;
    private List<Table> tables;
    private Wait wait;
    private boolean noWait;
    private boolean skipLocked;

    public ForMode getMode() {
        return mode;
    }

    public ForUpdateClause setMode(ForMode mode) {
        this.mode = mode;
        return this;
    }

    public List<Table> getTables() {
        return tables;
    }

    public ForUpdateClause setTables(List<Table> tables) {
        this.tables = tables;
        return this;
    }

    /**
     * Returns the first table from the OF clause, or {@code null} if none was specified.
     *
     * @return the first table, or {@code null}
     */
    public Table getFirstTable() {
        return (tables != null && !tables.isEmpty()) ? tables.get(0) : null;
    }

    public Wait getWait() {
        return wait;
    }

    public ForUpdateClause setWait(Wait wait) {
        this.wait = wait;
        return this;
    }

    public boolean isNoWait() {
        return noWait;
    }

    public ForUpdateClause setNoWait(boolean noWait) {
        this.noWait = noWait;
        return this;
    }

    public boolean isSkipLocked() {
        return skipLocked;
    }

    public ForUpdateClause setSkipLocked(boolean skipLocked) {
        this.skipLocked = skipLocked;
        return this;
    }

    /** Returns {@code true} when the mode is {@link ForMode#UPDATE}. */
    public boolean isForUpdate() {
        return mode == ForMode.UPDATE;
    }

    /** Returns {@code true} when the mode is {@link ForMode#SHARE}. */
    public boolean isForShare() {
        return mode == ForMode.SHARE;
    }

    /** Returns {@code true} when at least one table was listed in the OF clause. */
    public boolean hasTableList() {
        return tables != null && !tables.isEmpty();
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" FOR ").append(mode.getValue());
        if (tables != null && !tables.isEmpty()) {
            builder.append(" OF ");
            for (int i = 0; i < tables.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(tables.get(i));
            }
        }
        if (wait != null) {
            builder.append(wait);
        }
        if (noWait) {
            builder.append(" NOWAIT");
        } else if (skipLocked) {
            builder.append(" SKIP LOCKED");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
