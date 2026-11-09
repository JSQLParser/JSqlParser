/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.notify;

import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code LISTEN channel} and {@code UNLISTEN {channel | *}}, which register the session
 * as a listener on a channel that {@link NotifyStatement} signals and remove it again.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-listen.html">LISTEN</a>
 * @see <a href="https://www.postgresql.org/docs/18/sql-unlisten.html">UNLISTEN</a>
 */
public class ListenStatement extends ASTNodeAccessImpl implements Statement {
    private Action action = Action.LISTEN;
    private String channel;

    public enum Action {
        /** LISTEN channel */
        LISTEN,
        /** UNLISTEN {channel | *} */
        UNLISTEN
    }

    public ListenStatement() {}

    public ListenStatement(Action action, String channel) {
        setAction(action);
        setChannel(channel);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = Objects.requireNonNull(action, "action");
    }

    public ListenStatement withAction(Action action) {
        setAction(action);
        return this;
    }

    /** The channel name as written, or null for {@code UNLISTEN *}. */
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public ListenStatement withChannel(String channel) {
        setChannel(channel);
        return this;
    }

    /** Whether the statement is {@code UNLISTEN *}, which names no channel. */
    public boolean isAllChannels() {
        return channel == null;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(action.name()).append(' ').append(channel == null ? "*" : channel);
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
