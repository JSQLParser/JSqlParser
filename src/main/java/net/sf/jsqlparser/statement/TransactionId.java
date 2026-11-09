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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * The identifier of a transaction that two-phase commit statements name: MySQL's XA xid
 * {@code gtrid [, bqual [, formatID]]}, and the single string of PostgreSQL's
 * {@code PREPARE TRANSACTION}, {@code COMMIT PREPARED} and {@code ROLLBACK PREPARED} and of
 * Oracle's {@code ROLLBACK FORCE}, which has only the global part.
 *
 * <p>
 * The global transaction identifier and the branch qualifier are literals as written: a
 * {@link net.sf.jsqlparser.expression.StringValue}, or in MySQL also a
 * {@link net.sf.jsqlparser.expression.HexValue} or a bit-value {@code StringValue} with the
 * {@code B} prefix.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/xa-statements.html">MySQL XA Transaction
 *      SQL Statements</a>
 * @see <a href="https://www.postgresql.org/docs/current/sql-prepare-transaction.html">PostgreSQL
 *      PREPARE TRANSACTION</a>
 */
public class TransactionId extends ASTNodeAccessImpl {
    private Expression globalTransactionId;
    private Expression branchQualifier;
    private Long formatId;

    public TransactionId(Expression globalTransactionId) {
        setGlobalTransactionId(globalTransactionId);
    }

    public TransactionId(Expression globalTransactionId, Expression branchQualifier,
            Long formatId) {
        this(globalTransactionId);
        setBranchQualifier(branchQualifier);
        setFormatId(formatId);
    }

    /** The global transaction identifier: MySQL's gtrid, or the whole PostgreSQL identifier. */
    public Expression getGlobalTransactionId() {
        return globalTransactionId;
    }

    public void setGlobalTransactionId(Expression globalTransactionId) {
        this.globalTransactionId =
                Objects.requireNonNull(globalTransactionId, "globalTransactionId");
    }

    public TransactionId withGlobalTransactionId(Expression globalTransactionId) {
        setGlobalTransactionId(globalTransactionId);
        return this;
    }

    /** MySQL's bqual, or null when the xid has only a global transaction identifier. */
    public Expression getBranchQualifier() {
        return branchQualifier;
    }

    /**
     * Sets the branch qualifier; null removes it.
     *
     * @throws IllegalStateException if it is removed while a format ID is set
     */
    public void setBranchQualifier(Expression branchQualifier) {
        if (branchQualifier == null && formatId != null) {
            throw new IllegalStateException("A format ID requires a branch qualifier");
        }
        this.branchQualifier = branchQualifier;
    }

    public TransactionId withBranchQualifier(Expression branchQualifier) {
        setBranchQualifier(branchQualifier);
        return this;
    }

    /** MySQL's formatID, or null when the xid does not write one. */
    public Long getFormatId() {
        return formatId;
    }

    /**
     * Sets the format ID, written after the branch qualifier; null removes it.
     *
     * @throws IllegalArgumentException if it is negative
     * @throws IllegalStateException if it is set without a branch qualifier
     */
    public void setFormatId(Long formatId) {
        if (formatId != null) {
            if (formatId < 0) {
                throw new IllegalArgumentException("A format ID is unsigned: " + formatId);
            }
            if (branchQualifier == null) {
                throw new IllegalStateException("A format ID requires a branch qualifier");
            }
        }
        this.formatId = formatId;
    }

    public TransactionId withFormatId(Long formatId) {
        setFormatId(formatId);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(globalTransactionId);
        if (branchQualifier != null) {
            builder.append(", ").append(branchQualifier);
            if (formatId != null) {
                builder.append(", ").append(formatId);
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
