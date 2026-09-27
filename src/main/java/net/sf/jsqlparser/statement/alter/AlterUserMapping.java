/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.foreign.ForeignDataStatement;

public class AlterUserMapping extends ForeignDataStatement {
    private String user;
    private String server;

    public String getUser() {
        return user;
    }

    public void setUser(String value) {
        user = value;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String value) {
        server = value;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("ALTER USER MAPPING FOR ").append(user).append(" SERVER ").append(server);
        appendOptionsTo(sql, printer);
        return sql;
    }
}
