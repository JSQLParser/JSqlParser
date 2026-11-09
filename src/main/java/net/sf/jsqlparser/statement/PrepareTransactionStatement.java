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

import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * PostgreSQL {@code PREPARE TRANSACTION 'id'}, which prepares the current transaction for two-phase
 * commit. {@link Commit} and {@link RollbackStatement} complete it later with
 * {@code COMMIT PREPARED 'id'} and {@code ROLLBACK PREPARED 'id'}. Not to be confused with
 * {@link PrepareStatement}, which prepares a statement.
 *
 * @see <a href="https://www.postgresql.org/docs/current/sql-prepare-transaction.html">PostgreSQL
 *      PREPARE TRANSACTION</a>
 */
public class PrepareTransactionStatement extends ASTNodeAccessImpl implements Statement {
    private TransactionId transactionId;

    public PrepareTransactionStatement(TransactionId transactionId) {
        setTransactionId(transactionId);
    }

    /** The identifier that COMMIT PREPARED and ROLLBACK PREPARED name later. */
    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
    }

    public PrepareTransactionStatement withTransactionId(TransactionId transactionId) {
        setTransactionId(transactionId);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return transactionId.appendTo(builder.append("PREPARE TRANSACTION "));
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
