/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code DROP USER MAPPING [IF EXISTS] FOR user SERVER server}. A user mapping has no
 * name of its own, so it is identified by the user and the server, as in
 * {@link net.sf.jsqlparser.statement.create.usermapping.CreateUserMapping}.
 */
public class DropUserMapping implements Statement {
    private String user;
    private String server;
    private boolean ifExists;

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

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean value) {
        ifExists = value;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("DROP USER MAPPING ");
        if (ifExists) {
            sql.append("IF EXISTS ");
        }
        return sql.append("FOR ").append(user).append(" SERVER ").append(server);
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
