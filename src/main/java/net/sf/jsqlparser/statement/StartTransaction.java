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

/** START TRANSACTION, or BEGIN when a transaction dialect is selected. */
public class StartTransaction implements Statement {
    public enum Command {
        START_TRANSACTION, BEGIN
    }

    public enum Mode {
        ISOLATION_LEVEL_SERIALIZABLE(
                "ISOLATION LEVEL SERIALIZABLE"), ISOLATION_LEVEL_REPEATABLE_READ(
                        "ISOLATION LEVEL REPEATABLE READ"), ISOLATION_LEVEL_READ_COMMITTED(
                                "ISOLATION LEVEL READ COMMITTED"), ISOLATION_LEVEL_READ_UNCOMMITTED(
                                        "ISOLATION LEVEL READ UNCOMMITTED"), READ_WRITE(
                                                "READ WRITE"), READ_ONLY("READ ONLY"), DEFERRABLE(
                                                        "DEFERRABLE"), NOT_DEFERRABLE(
                                                                "NOT DEFERRABLE"), WITH_CONSISTENT_SNAPSHOT(
                                                                        "WITH CONSISTENT SNAPSHOT");

        private final String sql;

        Mode(String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            return sql;
        }
    }

    private Command command = Command.START_TRANSACTION;
    private TransactionKeyword keyword;
    private final List<Mode> modes = new ArrayList<>();

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = Objects.requireNonNull(command, "command");
    }

    public StartTransaction withCommand(Command command) {
        setCommand(command);
        return this;
    }

    public TransactionKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(TransactionKeyword keyword) {
        this.keyword = keyword;
    }

    public StartTransaction withKeyword(TransactionKeyword keyword) {
        setKeyword(keyword);
        return this;
    }

    /** Mutable, ordered transaction characteristics. */
    public List<Mode> getModes() {
        return modes;
    }

    public StartTransaction addMode(Mode mode) {
        modes.add(Objects.requireNonNull(mode, "mode"));
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(command == Command.BEGIN ? "BEGIN" : "START TRANSACTION");
        if (command == Command.BEGIN && keyword != null) {
            builder.append(' ').append(keyword);
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
