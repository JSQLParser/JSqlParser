/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.trigger;

import java.io.Serializable;
import net.sf.jsqlparser.expression.StringValue;

/** Structured account used by a MySQL trigger {@code DEFINER} clause. */
public class TriggerDefiner implements Serializable {

    private StringValue user;
    private StringValue host;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(user);
        if (host != null) {
            builder.append("@").append(host);
        }
        return builder.toString();
    }

    public TriggerDefiner withUser(StringValue user) {
        setUser(user);
        return this;
    }

    public TriggerDefiner withHost(StringValue host) {
        setHost(host);
        return this;
    }
}
