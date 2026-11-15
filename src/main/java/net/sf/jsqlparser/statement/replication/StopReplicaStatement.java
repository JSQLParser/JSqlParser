/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replication;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code STOP REPLICA [thread [, thread]] [FOR CHANNEL 'channel']}.<br>
 * Example:<br>
 * STOP REPLICA SQL_THREAD FOR CHANNEL 'c1'<br>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/stop-replica.html">MySQL STOP REPLICA</a>
 */
public class StopReplicaStatement extends ASTNodeAccessImpl implements Statement {
    private List<ReplicaThread> threads = new ArrayList<>();
    private StringValue channel;

    /** The threads to stop; empty for both. */
    public List<ReplicaThread> getThreads() {
        return threads;
    }

    public void setThreads(List<ReplicaThread> threads) {
        this.threads = threads;
    }

    /** The channel of FOR CHANNEL, or null for every channel. */
    public StringValue getChannel() {
        return channel;
    }

    public void setChannel(StringValue channel) {
        this.channel = channel;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("STOP REPLICA");
        ReplicaThread.appendTo(builder, threads);
        if (channel != null) {
            builder.append(" FOR CHANNEL ").append(channel);
        }
        return builder;
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
