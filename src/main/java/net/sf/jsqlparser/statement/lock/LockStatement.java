/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * Statement to lock one or more tables.<br>
 * Example:<br>
 * LOCK TABLE t IN EXCLUSIVE MODE<br>
 * <br>
 */
public class LockStatement implements Statement {

    public enum Scope {
        DEFAULT, ONLY, INCLUDING_DESCENDANTS
    }

    public static class Target {
        private Table table;
        private Scope scope = Scope.DEFAULT;

        public Target(Table table) {
            this.table = table;
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            this.table = table;
        }

        public Scope getScope() {
            return scope;
        }

        public void setScope(Scope scope) {
            this.scope = Objects.requireNonNull(scope);
        }

        @Override
        public String toString() {
            return (scope == Scope.ONLY ? "ONLY " : "") + table.getFullyQualifiedName()
                    + (scope == Scope.INCLUDING_DESCENDANTS ? " *" : "");
        }
    }

    private final List<Target> targets = new ArrayList<>();
    private boolean useTableKeyword = true;
    private LockMode lockMode;
    private boolean noWait;
    private Long waitSeconds;

    public LockStatement() {}

    /**
     * Creates a new LockStatement
     *
     * @param table The table to lock
     * @param lockMode The lock mode
     */
    public LockStatement(Table table, LockMode lockMode) {
        setTable(table);
        this.lockMode = lockMode;
    }

    public LockStatement(Table table, LockMode lockMode, boolean noWait, Long waitSeconds) {
        this(table, lockMode);
        this.noWait = noWait;
        this.waitSeconds = waitSeconds;
    }

    private void checkValidState() {
        if (noWait && waitSeconds != null) {
            throw new IllegalStateException(
                    "A LOCK statement cannot have NOWAIT and WAIT at the same time");
        }
    }

    /** Returns the first target for compatibility with the single-table API. */
    public Table getTable() {
        return targets.isEmpty() ? null : targets.get(0).getTable();
    }

    /** Replaces the first target table while preserving its scope and any further targets. */
    public void setTable(Table table) {
        if (targets.isEmpty()) {
            targets.add(new Target(table));
        } else {
            targets.get(0).setTable(table);
        }
    }

    public List<Target> getTargets() {
        return targets;
    }

    public boolean isUseTableKeyword() {
        return useTableKeyword;
    }

    public void setUseTableKeyword(boolean useTableKeyword) {
        this.useTableKeyword = useTableKeyword;
    }

    /** Returns null when IN ... MODE was omitted. */
    public LockMode getLockMode() {
        return lockMode;
    }

    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
    }

    /**
     * @return True if the statement has a NOWAIT clause
     */
    public boolean isNoWait() {
        return noWait;
    }

    /**
     * Sets the NOWAIT-Flag.
     *
     * @param noWait True if the statement should have the NOWAIT clause
     */
    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
        checkValidState();
    }

    /**
     * Sets the WAIT-Timeout. If this value is set, the Statement is rendered with WAIT
     * &lt;timeoutSeconds&gt;<br>
     * If the value is set to NULL, the WAIT-clause is skipped
     *
     * @param waitSeconds The number of seconds for the WAIT timeout or NULL to skip the WAIT clause
     */
    public void setWaitSeconds(Long waitSeconds) {
        this.waitSeconds = waitSeconds;
        checkValidState();
    }

    /**
     * @return The number of seconds in the WAIT clause, or NULL if the statement has no WAIT clause
     */
    public Long getWaitSeconds() {
        return waitSeconds;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(useTableKeyword ? "LOCK TABLE " : "LOCK ");
        for (int i = 0; i < targets.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(targets.get(i));
        }
        if (lockMode != null) {
            builder.append(" IN ").append(lockMode.getValue()).append(" MODE");
        }
        if (noWait) {
            builder.append(" NOWAIT");
        } else if (waitSeconds != null) {
            builder.append(" WAIT ").append(waitSeconds);
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
