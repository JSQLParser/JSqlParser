/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.lock;

import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code LOCK INSTANCE FOR BACKUP} and {@code UNLOCK INSTANCE}, which take and release the
 * instance-level backup lock. It locks no table, unlike {@link LockStatement}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/lock-instance-for-backup.html">MySQL LOCK
 *      INSTANCE FOR BACKUP and UNLOCK INSTANCE</a>
 */
public class BackupLockStatement extends ASTNodeAccessImpl implements Statement {
    private Action action = Action.LOCK;

    public enum Action {
        /** LOCK INSTANCE FOR BACKUP */
        LOCK("LOCK INSTANCE FOR BACKUP"),
        /** UNLOCK INSTANCE */
        UNLOCK("UNLOCK INSTANCE");

        private final String sql;

        Action(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    public BackupLockStatement() {}

    public BackupLockStatement(Action action) {
        setAction(action);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = Objects.requireNonNull(action);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(action.getSql());
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
