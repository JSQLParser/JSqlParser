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
 * The clauses shared by {@link Commit} and {@link RollbackStatement}:
 * {@code [WORK | TRANSACTION] ... [AND [NO] CHAIN] [[NO] RELEASE]}, or the identifier of a prepared
 * transaction that the statement completes instead of the current one. Null fields are omitted.
 */
public abstract class TransactionCompletionStatement extends ASTNodeAccessImpl
        implements Statement {
    /** The keyword before the identifier of a prepared transaction. */
    public enum PreparedTransactionKeyword {
        /** PostgreSQL {@code COMMIT PREPARED 'id'} and {@code ROLLBACK PREPARED 'id'}. */
        PREPARED,
        /**
         * Oracle {@code ROLLBACK [WORK] FORCE 'id'}, naming an in-doubt distributed transaction.
         */
        FORCE
    }

    private TransactionKeyword keyword;
    private TransactionChain chain;
    private TransactionRelease release;
    private PreparedTransactionKeyword preparedTransactionKeyword;
    private TransactionId preparedTransactionId;

    public TransactionKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(TransactionKeyword keyword) {
        this.keyword = keyword;
    }

    public TransactionChain getChain() {
        return chain;
    }

    public void setChain(TransactionChain chain) {
        this.chain = chain;
    }

    public TransactionRelease getRelease() {
        return release;
    }

    public void setRelease(TransactionRelease release) {
        this.release = release;
    }

    /** {@code PREPARED} or {@code FORCE} when a prepared transaction is named, otherwise null. */
    public PreparedTransactionKeyword getPreparedTransactionKeyword() {
        return preparedTransactionKeyword;
    }

    /**
     * The identifier of the prepared transaction to complete, written after
     * {@link #getPreparedTransactionKeyword()}, or null when the current transaction completes.
     */
    public TransactionId getPreparedTransactionId() {
        return preparedTransactionId;
    }

    /**
     * Names the prepared transaction to complete. A null identifier completes the current
     * transaction and clears the keyword.
     *
     * @throws NullPointerException if an identifier is given without a keyword
     */
    public void setPreparedTransaction(PreparedTransactionKeyword keyword, TransactionId id) {
        this.preparedTransactionKeyword =
                id == null ? null : Objects.requireNonNull(keyword, "keyword");
        this.preparedTransactionId = id;
    }

    /** Whether the statement is spelled END or ABORT, PostgreSQL's names of COMMIT and ROLLBACK. */
    public abstract boolean isPostgresSpelling();

    public abstract StringBuilder appendTo(StringBuilder builder);

    /** Appends {@code WORK} or {@code TRANSACTION} after the command. */
    protected StringBuilder appendKeyword(StringBuilder builder) {
        return keyword == null ? builder : builder.append(' ').append(keyword);
    }

    /** Appends {@code PREPARED 'id'} or {@code FORCE 'id'} when a prepared transaction is named. */
    protected StringBuilder appendPreparedTransaction(StringBuilder builder) {
        if (preparedTransactionId != null) {
            builder.append(' ').append(preparedTransactionKeyword).append(' ');
            preparedTransactionId.appendTo(builder);
        }
        return builder;
    }

    /** Appends {@code AND [NO] CHAIN} and {@code [NO] RELEASE}, which end the statement. */
    protected StringBuilder appendCompletion(StringBuilder builder) {
        if (chain != null) {
            builder.append(' ').append(chain);
        }
        if (release != null) {
            builder.append(' ').append(release);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
