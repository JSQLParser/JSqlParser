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
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code START REPLICA [thread [, thread]] [UNTIL condition [, condition] ...] [USER = 'u']
 * [PASSWORD = 'p'] [DEFAULT_AUTH = 'a'] [PLUGIN_DIR = 'd'] [FOR CHANNEL 'channel']}.<br>
 * Example:<br>
 * START REPLICA SQL_THREAD UNTIL SQL_BEFORE_GTIDS = 'uuid:1-5' FOR CHANNEL 'c1'<br>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/start-replica.html">MySQL START REPLICA</a>
 */
public class StartReplicaStatement extends ASTNodeAccessImpl implements Statement {
    private List<ReplicaThread> threads = new ArrayList<>();
    private List<ReplicaUntilOption> untilOptions = new ArrayList<>();
    private StringValue user;
    private StringValue password;
    private StringValue defaultAuth;
    private StringValue pluginDir;
    private StringValue channel;

    /** The threads to start; empty for both. */
    public List<ReplicaThread> getThreads() {
        return threads;
    }

    public void setThreads(List<ReplicaThread> threads) {
        this.threads = threads;
    }

    /** The conditions of UNTIL; empty without an UNTIL clause. */
    public List<ReplicaUntilOption> getUntilOptions() {
        return untilOptions;
    }

    public void setUntilOptions(List<ReplicaUntilOption> untilOptions) {
        this.untilOptions = untilOptions;
    }

    public StringValue getUser() {
        return user;
    }

    public void setUser(StringValue user) {
        this.user = user;
    }

    public StringValue getPassword() {
        return password;
    }

    public void setPassword(StringValue password) {
        this.password = password;
    }

    public StringValue getDefaultAuth() {
        return defaultAuth;
    }

    public void setDefaultAuth(StringValue defaultAuth) {
        this.defaultAuth = defaultAuth;
    }

    public StringValue getPluginDir() {
        return pluginDir;
    }

    public void setPluginDir(StringValue pluginDir) {
        this.pluginDir = pluginDir;
    }

    /** The channel of FOR CHANNEL, or null for every channel. */
    public StringValue getChannel() {
        return channel;
    }

    public void setChannel(StringValue channel) {
        this.channel = channel;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
        builder.append("START REPLICA");
        ReplicaThread.appendTo(builder, threads);
        for (int i = 0; i < untilOptions.size(); i++) {
            builder.append(i == 0 ? " UNTIL " : ", ");
            untilOptions.get(i).appendTo(builder, expressions);
        }
        appendConnectionOption(builder, "USER", user);
        appendConnectionOption(builder, "PASSWORD", password);
        appendConnectionOption(builder, "DEFAULT_AUTH", defaultAuth);
        appendConnectionOption(builder, "PLUGIN_DIR", pluginDir);
        if (channel != null) {
            builder.append(" FOR CHANNEL ").append(channel);
        }
        return builder;
    }

    private static void appendConnectionOption(StringBuilder builder, String name,
            StringValue value) {
        if (value != null) {
            builder.append(' ').append(name).append(" = ").append(value);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
