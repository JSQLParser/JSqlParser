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
 * MySQL {@code CHANGE REPLICATION SOURCE TO option [, option] ... [FOR CHANNEL 'channel']}.<br>
 * Example:<br>
 * CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h', SOURCE_PORT = 3306 FOR CHANNEL 'c1'<br>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/change-replication-source-to.html">MySQL
 *      CHANGE REPLICATION SOURCE TO</a>
 */
public class ChangeReplicationSourceStatement extends ASTNodeAccessImpl implements Statement {
    private List<ReplicationSourceOption> options = new ArrayList<>();
    private StringValue channel;

    public List<ReplicationSourceOption> getOptions() {
        return options;
    }

    public void setOptions(List<ReplicationSourceOption> options) {
        this.options = options;
    }

    /** The channel of FOR CHANNEL, or null for the default channel. */
    public StringValue getChannel() {
        return channel;
    }

    public void setChannel(StringValue channel) {
        this.channel = channel;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
        builder.append("CHANGE REPLICATION SOURCE TO ");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            options.get(i).appendTo(builder, expressions);
        }
        if (channel != null) {
            builder.append(" FOR CHANNEL ").append(channel);
        }
        return builder;
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
