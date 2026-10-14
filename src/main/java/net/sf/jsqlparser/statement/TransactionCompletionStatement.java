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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * The clauses shared by {@link Commit} and {@link RollbackStatement}:
 * {@code [WORK | TRANSACTION] ... [AND [NO] CHAIN] [[NO] RELEASE]}. Null fields are omitted.
 */
public abstract class TransactionCompletionStatement extends ASTNodeAccessImpl
        implements Statement {
    private TransactionKeyword keyword;
    private TransactionChain chain;
    private TransactionRelease release;

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

    /** Whether the statement is spelled END or ABORT, PostgreSQL's names of COMMIT and ROLLBACK. */
    public abstract boolean isPostgresSpelling();

    public abstract StringBuilder appendTo(StringBuilder builder);

    /** Appends {@code WORK} or {@code TRANSACTION} after the command. */
    protected StringBuilder appendKeyword(StringBuilder builder) {
        return keyword == null ? builder : builder.append(' ').append(keyword);
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
