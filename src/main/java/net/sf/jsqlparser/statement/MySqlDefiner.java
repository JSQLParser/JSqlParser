/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.io.Serializable;
import net.sf.jsqlparser.expression.StringValue;

/** Structured account shared by MySQL stored-object {@code DEFINER} clause. */
public class MySqlDefiner implements Serializable {

    private StringValue user;
    private StringValue host;
    private boolean currentUserParentheses;

    public boolean isCurrentUserParentheses() {
        return currentUserParentheses;
    }

    public void setCurrentUserParentheses(boolean currentUserParentheses) {
        this.currentUserParentheses = currentUserParentheses;
    }


    public StringValue getUser() {
        return user;
    }

    public void setUser(StringValue user) {
        this.user = user;
        currentUserParentheses = false;
    }

    public StringValue getHost() {
        return host;
    }

    public void setHost(StringValue host) {
        this.host = host;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(user);
        if (currentUserParentheses) {
            builder.append("()");
        }
        if (host != null) {
            builder.append("@").append(host);
        }
        return builder.toString();
    }

    public MySqlDefiner withUser(StringValue user) {
        setUser(user);
        return this;
    }

    public MySqlDefiner withHost(StringValue host) {
        setHost(host);
        return this;
    }
}
