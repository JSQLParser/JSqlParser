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

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL NOTIFY, usable independently and in CREATE RULE actions. */
public class NotifyStatement implements Statement {
    private String channel;
    private StringValue payload;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String value) {
        channel = value;
    }

    public StringValue getPayload() {
        return payload;
    }

    public void setPayload(StringValue value) {
        payload = value;
    }

    public void visitExpressions(Consumer<Expression> visitor) {
        if (payload != null) {
            visitor.accept(payload);
        }
    }

    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("NOTIFY ").append(channel);
        if (payload != null) {
            sql.append(", ");
            printer.accept(payload);
        }
        return sql;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        return appendTo(sql, sql::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
