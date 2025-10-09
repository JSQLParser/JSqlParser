package net.sf.jsqlparser.statement.lock;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * Statement to Lock a specific table.<br>
 * Example:<br>
 * LOCK TABLE t IN EXCLUSIVE MODE<br>
 * <br>
 */
public class LockStatement implements Statement {

    private Table table;
    private LockMode lockMode;
    private boolean noWait;
    private Long waitSeconds;

    /**
     * Creates a new LockStatement
     *
     * @param table The table to lock
     * @param lockMode The lock mode
     */
    public LockStatement(Table table, LockMode lockMode) {
        this.table = table;
        this.lockMode = lockMode;
    }

    public LockStatement(Table table, LockMode lockMode, boolean noWait, Long waitSeconds) {
        this(table, lockMode);
        this.table = table;
        this.lockMode = lockMode;
        this.noWait = noWait;
        this.waitSeconds = waitSeconds;
    }

    private void checkValidState() {
        if (noWait && waitSeconds != null) {
            throw new IllegalStateException(
                    "A LOCK statement cannot have NOWAIT and WAIT at the same time");
        }
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

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

    @Override
    public String toString() {
        return "LOCK TABLE "
                + table.getFullyQualifiedName()
                + " IN "
                + lockMode.getValue()
                + " MODE"
                + (noWait ? " NOWAIT" : "")
                + (waitSeconds != null ? " WAIT " + waitSeconds : "");
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

}
