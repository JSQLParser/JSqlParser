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

import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code ALTER INSTANCE}, which acts on the server instance:
 * {@code {ENABLE | DISABLE} INNODB REDO_LOG}, {@code ROTATE INNODB MASTER KEY},
 * {@code ROTATE BINLOG MASTER KEY}, {@code RELOAD TLS [FOR CHANNEL channel] [NO ROLLBACK ON ERROR]}
 * and {@code RELOAD KEYRING}.
 *
 * <p>
 * MySQL reads INNODB, BINLOG and REDO_LOG as identifiers, so they may be quoted; the statement
 * renders them as plain words. The channel is kept as written: MySQL's grammar takes any
 * identifier, and the server accepts {@code mysql_main} and {@code mysql_admin} when it runs the
 * statement.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/alter-instance.html">MySQL ALTER
 *      INSTANCE</a>
 */
public class AlterInstance extends ASTNodeAccessImpl implements Statement {
    /** The words after ALTER INSTANCE, without the options of RELOAD TLS. */
    public enum Action {
        ENABLE_INNODB_REDO_LOG("ENABLE INNODB REDO_LOG"), DISABLE_INNODB_REDO_LOG(
                "DISABLE INNODB REDO_LOG"), ROTATE_INNODB_MASTER_KEY(
                        "ROTATE INNODB MASTER KEY"), ROTATE_BINLOG_MASTER_KEY(
                                "ROTATE BINLOG MASTER KEY"), RELOAD_TLS(
                                        "RELOAD TLS"), RELOAD_KEYRING("RELOAD KEYRING");

        private final String sql;

        Action(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    private Action action;
    private String channel;
    private boolean noRollbackOnError;

    public AlterInstance(Action action) {
        this.action = Objects.requireNonNull(action, "action");
    }

    public Action getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @throws IllegalArgumentException if the statement has options of RELOAD TLS and the action is
     *         another one
     */
    public void setAction(Action action) {
        Objects.requireNonNull(action, "action");
        if (action != Action.RELOAD_TLS && (channel != null || noRollbackOnError)) {
            throw new IllegalArgumentException(
                    "FOR CHANNEL and NO ROLLBACK ON ERROR belong to RELOAD TLS");
        }
        this.action = action;
    }

    public AlterInstance withAction(Action action) {
        setAction(action);
        return this;
    }

    /** The identifier of {@code RELOAD TLS FOR CHANNEL channel} as written, or null. */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the channel of RELOAD TLS; null removes the clause.
     *
     * @throws IllegalArgumentException if the action is not RELOAD TLS
     */
    public void setChannel(String channel) {
        if (channel != null && action != Action.RELOAD_TLS) {
            throw new IllegalArgumentException("FOR CHANNEL belongs to RELOAD TLS");
        }
        this.channel = channel;
    }

    public AlterInstance withChannel(String channel) {
        setChannel(channel);
        return this;
    }

    /** Whether RELOAD TLS ends with NO ROLLBACK ON ERROR. */
    public boolean isNoRollbackOnError() {
        return noRollbackOnError;
    }

    /**
     * Sets NO ROLLBACK ON ERROR.
     *
     * @throws IllegalArgumentException if the action is not RELOAD TLS
     */
    public void setNoRollbackOnError(boolean noRollbackOnError) {
        if (noRollbackOnError && action != Action.RELOAD_TLS) {
            throw new IllegalArgumentException("NO ROLLBACK ON ERROR belongs to RELOAD TLS");
        }
        this.noRollbackOnError = noRollbackOnError;
    }

    public AlterInstance withNoRollbackOnError(boolean noRollbackOnError) {
        setNoRollbackOnError(noRollbackOnError);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER INSTANCE ").append(action.getSql());
        if (channel != null) {
            builder.append(" FOR CHANNEL ").append(channel);
        }
        if (noRollbackOnError) {
            builder.append(" NO ROLLBACK ON ERROR");
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
