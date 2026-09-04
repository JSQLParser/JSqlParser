/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.user;

import java.io.Serializable;
import net.sf.jsqlparser.expression.StringValue;

/** A user and optional host together with its authentication clause. */
public class UserAccount implements Serializable {

    private StringValue user;
    private StringValue host;
    private UserAuthentication authentication;

    public StringValue getUser() {
        return user;
    }

    public void setUser(StringValue user) {
        this.user = user;
    }

    public StringValue getHost() {
        return host;
    }

    public void setHost(StringValue host) {
        this.host = host;
    }

    public UserAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(UserAuthentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(user);
        if (host != null) {
            builder.append("@").append(host);
        }
        if (authentication != null) {
            builder.append(" ").append(authentication);
        }
        return builder.toString();
    }

    public UserAccount withUser(StringValue user) {
        setUser(user);
        return this;
    }

    public UserAccount withHost(StringValue host) {
        setHost(host);
        return this;
    }

    public UserAccount withAuthentication(UserAuthentication authentication) {
        setAuthentication(authentication);
        return this;
    }
}
