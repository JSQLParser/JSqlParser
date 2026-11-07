/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Transaction characteristics set without starting a transaction:
 *
 * <ul>
 * <li>{@code SET [GLOBAL | SESSION | LOCAL | PERSIST | PERSIST_ONLY] TRANSACTION mode, ...} (MySQL;
 * MariaDB without PERSIST and PERSIST_ONLY; PostgreSQL with SESSION or LOCAL only),</li>
 * <li>{@code SET [SESSION | LOCAL] SESSION CHARACTERISTICS AS TRANSACTION mode ...}
 * (PostgreSQL),</li>
 * <li>{@code SET [SESSION | LOCAL] TRANSACTION SNAPSHOT 'id'} (PostgreSQL).</li>
 * </ul>
 *
 * The modes are the {@link TransactionMode} values of BEGIN and START TRANSACTION.
 */
public class SetTransactionStatement extends ASTNodeAccessImpl implements Statement {
    private final List<TransactionMode> modes = new ArrayList<>();
    private SetScope scope = SetScope.NONE;
    private boolean sessionCharacteristics;
    private StringValue snapshotId;

    /**
     * The scope keyword written directly after SET. In MySQL, {@link SetScope#NONE} applies to the
     * next transaction, {@link SetScope#SESSION} and its synonym {@link SetScope#LOCAL} to the
     * session, {@link SetScope#GLOBAL} to later sessions, and {@link SetScope#PERSIST} and
     * {@link SetScope#PERSIST_ONLY} also or only to the persisted configuration. PostgreSQL accepts
     * SESSION and LOCAL before every form; the SESSION CHARACTERISTICS form, which
     * {@link #isSessionCharacteristics()} marks, sets the session default whatever the scope.
     */
    public SetScope getScope() {
        return scope;
    }

    /** Sets the scope keyword; null means {@link SetScope#NONE}. */
    public void setScope(SetScope scope) {
        this.scope = scope == null ? SetScope.NONE : scope;
    }

    public SetTransactionStatement withScope(SetScope scope) {
        setScope(scope);
        return this;
    }

    /**
     * Whether the PostgreSQL form {@code SESSION CHARACTERISTICS AS TRANSACTION} follows SET and
     * the scope, as in {@code SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY}.
     */
    public boolean isSessionCharacteristics() {
        return sessionCharacteristics;
    }

    /** Selects the SESSION CHARACTERISTICS form; the scope keyword before it is kept. */
    public void setSessionCharacteristics(boolean sessionCharacteristics) {
        this.sessionCharacteristics = sessionCharacteristics;
    }

    public SetTransactionStatement withSessionCharacteristics(boolean sessionCharacteristics) {
        setSessionCharacteristics(sessionCharacteristics);
        return this;
    }

    /** Mutable, ordered transaction characteristics; empty for SET TRANSACTION SNAPSHOT. */
    public List<TransactionMode> getModes() {
        return modes;
    }

    public SetTransactionStatement addMode(TransactionMode mode) {
        modes.add(Objects.requireNonNull(mode, "mode"));
        return this;
    }

    /** The snapshot of SET TRANSACTION SNAPSHOT, or null when modes are set instead. */
    public StringValue getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(StringValue snapshotId) {
        this.snapshotId = snapshotId;
    }

    public SetTransactionStatement withSnapshotId(StringValue snapshotId) {
        setSnapshotId(snapshotId);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("SET ");
        if (scope != SetScope.NONE) {
            builder.append(scope).append(' ');
        }
        if (sessionCharacteristics) {
            builder.append("SESSION CHARACTERISTICS AS ");
        }
        builder.append("TRANSACTION");
        if (snapshotId != null) {
            builder.append(" SNAPSHOT ").append(snapshotId);
        }
        for (int i = 0; i < modes.size(); i++) {
            builder.append(i == 0 ? " " : ", ").append(modes.get(i));
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
