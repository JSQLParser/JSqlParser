/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.admin;

import java.util.Locale;
import java.util.Objects;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * One flush option of MySQL {@code FLUSH}, such as {@code BINARY LOGS} or
 * {@code RELAY LOGS FOR CHANNEL 'c1'}.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/flush.html">MySQL FLUSH</a>
 */
public class FlushOption extends ASTNodeAccessImpl {
    private Kind kind;
    private StringValue channel;

    /** The flush options of MySQL 8.4. */
    public enum Kind {
        BINARY_LOGS, ENGINE_LOGS, ERROR_LOGS, GENERAL_LOGS, LOGS, OPTIMIZER_COSTS, PRIVILEGES, RELAY_LOGS, SLOW_LOGS, STATUS, USER_RESOURCES;

        /** Returns the SQL spelling, for example {@code BINARY LOGS}. */
        public String getKeyword() {
            return this == OPTIMIZER_COSTS || this == USER_RESOURCES ? name()
                    : name().replace('_', ' ');
        }

        /**
         * Returns the option spelled by the keyword, ignoring case, or null.
         *
         * @param keyword the words of the option separated by one space, such as
         *        {@code binary logs}
         */
        public static Kind fromKeyword(String keyword) {
            String upper = keyword == null ? null : keyword.toUpperCase(Locale.ROOT);
            for (Kind kind : values()) {
                if (kind.getKeyword().equals(upper)) {
                    return kind;
                }
            }
            return null;
        }
    }

    public FlushOption(Kind kind) {
        setKind(kind);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = Objects.requireNonNull(kind);
    }

    public FlushOption withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    /** Returns the channel of {@code RELAY LOGS FOR CHANNEL 'name'}, or null. */
    public StringValue getChannel() {
        return channel;
    }

    public void setChannel(StringValue channel) {
        this.channel = channel;
    }

    public FlushOption withChannel(StringValue channel) {
        setChannel(channel);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(kind.getKeyword());
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
