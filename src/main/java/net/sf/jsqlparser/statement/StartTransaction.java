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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** START TRANSACTION, or BEGIN when a transaction dialect is selected. */
public class StartTransaction extends ASTNodeAccessImpl implements Statement {
    public enum Command {
        START_TRANSACTION, BEGIN
    }

    private final List<TransactionMode> modes = new ArrayList<>();
    private Command command = Command.START_TRANSACTION;
    private TransactionKeyword keyword;

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

    /** The statement context of the modes, which decides the modes each dialect accepts. */
    public TransactionMode.Context getModeContext() {
        return command == Command.BEGIN ? TransactionMode.Context.BEGIN
                : TransactionMode.Context.START_TRANSACTION;
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
    public List<TransactionMode> getModes() {
        return modes;
    }

    public StartTransaction addMode(TransactionMode mode) {
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
