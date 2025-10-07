package net.sf.jsqlparser.statement.lock;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * Statement to Lock a specific table.<br>
 * Example:<br>
 * LOCK TABLE &lt;TABLE&gt; IN EXCLUSIVE MODE;<br>
 * <br>
 */
public class LockStatement implements Statement {

	private Table table;
	private LockMode lockMode;
	private boolean noWait;
	private Long waitSeconds;

	public LockStatement(Table table, LockMode lockMode, boolean noWait, Long waitSeconds) {
		this.table = table;
		this.lockMode = lockMode;
		this.noWait = noWait;
		this.waitSeconds = waitSeconds;
	}

	private void checkValidState() {
		if (noWait && waitSeconds != null) {
			throw new IllegalStateException("A LOCK statement cannot have NOWAIT and WAIT at the same time");
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

	public boolean isNoWait() {
		return noWait;
	}

	/**
	 * Sets the NOWAIT-Flag. Clears a WAIT-Timeout if one was set before.
	 *
	 * @param noWait The new value of the NOWAIT-Flag
	 */
	public void setNoWait(boolean noWait) {
		this.waitSeconds = null;
		this.noWait = noWait;
		checkValidState();
	}

	public boolean isWait() {
		return waitSeconds != null;
	}

	/**
	 * Sets the WAIT-Timeout. If this value is set, the Statement is rendered with WAIT &lt;timeoutSeconds&gt;
	 *
	 * @param waitSeconds The number of seconds for the WAIT timeout
	 */
	public void setWaitSeconds(long waitSeconds) {
		this.noWait = false;
		this.waitSeconds = waitSeconds;
		checkValidState();
	}

	@Override
	public String toString() {
		return
			"LOCK TABLE "
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

	public long getWaitTimeout() {
		return waitSeconds;
	}
}
