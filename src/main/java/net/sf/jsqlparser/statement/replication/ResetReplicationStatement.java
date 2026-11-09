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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * MySQL {@code RESET reset_option [, reset_option] ...} with the replication options
 * {@code BINARY LOGS AND GTIDS [TO n]} and {@code REPLICA [ALL] [FOR CHANNEL 'channel']}. The
 * PostgreSQL {@code RESET} of a configuration parameter remains a
 * {@link net.sf.jsqlparser.statement.ResetStatement}.<br>
 * Example:<br>
 * RESET REPLICA ALL FOR CHANNEL 'c1', BINARY LOGS AND GTIDS TO 1234<br>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/reset.html">MySQL RESET</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/reset-binary-logs-and-gtids.html">MySQL
 *      RESET BINARY LOGS AND GTIDS</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/reset-replica.html">MySQL RESET REPLICA</a>
 */
public class ResetReplicationStatement extends ASTNodeAccessImpl implements Statement {
    private List<Option> options = new ArrayList<>();

    public enum Kind {
        BINARY_LOGS_AND_GTIDS("BINARY LOGS AND GTIDS"), REPLICA("REPLICA");

        private final String sql;

        Kind(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    /** One reset option: what it resets and the clauses that kind takes. */
    public static class Option implements Serializable {
        private final Kind kind;
        private LongValue binaryLogIndex;
        private boolean all;
        private StringValue channel;

        public Option(Kind kind) {
            this.kind = Objects.requireNonNull(kind);
        }

        public Kind getKind() {
            return kind;
        }

        /** The number of the first binary log file after BINARY LOGS AND GTIDS TO, or null. */
        public LongValue getBinaryLogIndex() {
            return binaryLogIndex;
        }

        public void setBinaryLogIndex(LongValue binaryLogIndex) {
            this.binaryLogIndex = binaryLogIndex;
        }

        /** Whether REPLICA ALL also clears the connection parameters. */
        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        /** The channel of REPLICA ... FOR CHANNEL, or null for every channel. */
        public StringValue getChannel() {
            return channel;
        }

        public void setChannel(StringValue channel) {
            this.channel = channel;
        }

        public StringBuilder appendTo(StringBuilder builder) {
            builder.append(kind.getSql());
            if (binaryLogIndex != null) {
                builder.append(" TO ").append(binaryLogIndex);
            }
            if (all) {
                builder.append(" ALL");
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
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("RESET ");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            options.get(i).appendTo(builder);
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
