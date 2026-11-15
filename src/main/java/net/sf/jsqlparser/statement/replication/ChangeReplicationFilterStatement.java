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
 * MySQL {@code CHANGE REPLICATION FILTER filter [, filter] ... [FOR CHANNEL 'channel']}.<br>
 * Example:<br>
 * CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1, d2), REPLICATE_REWRITE_DB = ((a, b))<br>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/change-replication-filter.html">MySQL
 *      CHANGE REPLICATION FILTER</a>
 */
public class ChangeReplicationFilterStatement extends ASTNodeAccessImpl implements Statement {
    private List<ReplicationFilter> filters = new ArrayList<>();
    private StringValue channel;

    public List<ReplicationFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<ReplicationFilter> filters) {
        this.filters = filters;
    }

    /** The channel of FOR CHANNEL, or null when the filters apply to every channel. */
    public StringValue getChannel() {
        return channel;
    }

    public void setChannel(StringValue channel) {
        this.channel = channel;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CHANGE REPLICATION FILTER ");
        for (int i = 0; i < filters.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            filters.get(i).appendTo(builder);
        }
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
