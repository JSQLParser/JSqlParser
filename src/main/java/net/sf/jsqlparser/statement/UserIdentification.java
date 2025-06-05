/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;

import java.io.Serializable;

public class UserIdentification implements Serializable {
    private StringValue user;
    private StringValue password;

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

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append("USER ");
        sql.append(user);

        sql.append(" IDENTIFIED BY ");
        sql.append(password);

        return sql.toString();
    }
}
