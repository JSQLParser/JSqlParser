/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Objects;

/**
 * COMMIT, or its PostgreSQL spelling END. PostgreSQL {@code COMMIT PREPARED 'id'} commits a
 * prepared transaction instead of the current one.
 */
public class Commit extends TransactionCompletionStatement {
    /** The keyword that starts the statement; both commit the current transaction. */
    public enum Command {
        COMMIT, END
    }

    private Command command = Command.COMMIT;

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = Objects.requireNonNull(command, "command");
    }

    public Commit withCommand(Command command) {
        setCommand(command);
        return this;
    }

    public Commit withKeyword(TransactionKeyword keyword) {
        setKeyword(keyword);
        return this;
    }

    public Commit withChain(TransactionChain chain) {
        setChain(chain);
        return this;
    }

    public Commit withRelease(TransactionRelease release) {
        setRelease(release);
        return this;
    }

    /**
     * Names the prepared transaction of PostgreSQL {@code COMMIT PREPARED 'id'}; null commits the
     * current transaction.
     */
    public Commit withPreparedTransaction(TransactionId id) {
        setPreparedTransaction(PreparedTransactionKeyword.PREPARED, id);
        return this;
    }

    @Override
    public boolean isPostgresSpelling() {
        return command == Command.END;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(command);
        appendKeyword(builder);
        appendPreparedTransaction(builder);
        return appendCompletion(builder);
    }
}
