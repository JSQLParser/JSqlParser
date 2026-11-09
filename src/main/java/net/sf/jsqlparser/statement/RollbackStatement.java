/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
/*
 * Copyright (C) 2021 JSQLParser.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package net.sf.jsqlparser.statement;

import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;

/**
 * ROLLBACK, or its PostgreSQL spelling ABORT. PostgreSQL {@code ROLLBACK PREPARED 'id'} and Oracle
 * {@code ROLLBACK FORCE 'id'} roll back a prepared transaction instead of the current one.
 *
 * @author are
 */
public class RollbackStatement extends TransactionCompletionStatement {
    /** The keyword that starts the statement; ABORT has no savepoint, FORCE or PREPARED form. */
    public enum Command {
        ROLLBACK, ABORT
    }

    private Command command = Command.ROLLBACK;
    private boolean usingSavepointKeyword = false;
    private String savepointName = null;

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = Objects.requireNonNull(command, "command");
    }

    public RollbackStatement withCommand(Command command) {
        setCommand(command);
        return this;
    }

    public RollbackStatement withKeyword(TransactionKeyword keyword) {
        setKeyword(keyword);
        return this;
    }

    public RollbackStatement withChain(TransactionChain chain) {
        setChain(chain);
        return this;
    }

    public RollbackStatement withRelease(TransactionRelease release) {
        setRelease(release);
        return this;
    }

    /**
     * Names the prepared transaction of PostgreSQL {@code ROLLBACK PREPARED 'id'} or Oracle
     * {@code ROLLBACK FORCE 'id'}; a null identifier rolls back the current transaction.
     */
    public RollbackStatement withPreparedTransaction(PreparedTransactionKeyword keyword,
            TransactionId id) {
        setPreparedTransaction(keyword, id);
        return this;
    }

    public boolean isUsingWorkKeyword() {
        return getKeyword() == TransactionKeyword.WORK;
    }

    public void setUsingWorkKeyword(boolean usingWorkKeyword) {
        if (usingWorkKeyword) {
            setKeyword(TransactionKeyword.WORK);
        } else if (getKeyword() == TransactionKeyword.WORK) {
            setKeyword(null);
        }
    }

    public RollbackStatement withUsingWorkKeyword(boolean usingWorkKeyword) {
        setUsingWorkKeyword(usingWorkKeyword);
        return this;
    }

    public boolean isUsingSavepointKeyword() {
        return usingSavepointKeyword;
    }

    public void setUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
    }

    public RollbackStatement withUsingSavepointKeyword(boolean usingSavepointKeyword) {
        this.usingSavepointKeyword = usingSavepointKeyword;
        return this;
    }

    public String getSavepointName() {
        return savepointName;
    }

    public void setSavepointName(String savepointName) {
        this.savepointName = savepointName;
    }

    public RollbackStatement withSavepointName(String savepointName) {
        this.savepointName = savepointName;
        return this;
    }

    /**
     * The identifier of Oracle {@code ROLLBACK FORCE 'id'} as written, with its quotes, or null.
     * Derived from {@link #getPreparedTransactionId()} when the keyword is
     * {@link PreparedTransactionKeyword#FORCE}.
     */
    public String getForceDistributedTransactionIdentifier() {
        return getPreparedTransactionKeyword() == PreparedTransactionKeyword.FORCE
                ? getPreparedTransactionId().toString()
                : null;
    }

    /**
     * Sets the identifier of Oracle {@code ROLLBACK FORCE 'id'} as it is to be written. It is
     * stored as the {@link StringValue} of {@link #getPreparedTransactionId()}; text that is not a
     * string literal is kept verbatim. Null or blank removes a FORCE identifier.
     */
    public void setForceDistributedTransactionIdentifier(
            String forceDistributedTransactionIdentifier) {
        if (forceDistributedTransactionIdentifier != null
                && !forceDistributedTransactionIdentifier.trim().isEmpty()) {
            setPreparedTransaction(PreparedTransactionKeyword.FORCE,
                    new TransactionId(asWritten(forceDistributedTransactionIdentifier)));
        } else if (getPreparedTransactionKeyword() == PreparedTransactionKeyword.FORCE) {
            setPreparedTransaction(null, null);
        }
    }

    /** The literal {@code text} spells, or the text itself when it is not a string literal. */
    private static StringValue asWritten(String text) {
        StringValue literal = new StringValue(text);
        return literal.toString().equals(text) ? literal
                : new StringValue().withValue(text).setQuoteStr("");
    }

    public RollbackStatement withForceDistributedTransactionIdentifier(
            String forceDistributedTransactionIdentifier) {
        setForceDistributedTransactionIdentifier(forceDistributedTransactionIdentifier);
        return this;
    }

    @Override
    public boolean isPostgresSpelling() {
        return command == Command.ABORT;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(command);
        appendKeyword(builder);
        if (savepointName != null && !savepointName.trim().isEmpty()) {
            builder.append(" TO ").append(usingSavepointKeyword ? "SAVEPOINT " : "")
                    .append(savepointName);
        } else {
            appendPreparedTransaction(builder);
        }
        return appendCompletion(builder);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

}
