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

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.MySqlDefiner;

/** Retains the trigger API while sharing MySQL DEFINER account handling with views. */
public class TriggerDefiner extends MySqlDefiner {
    @Override
    public TriggerDefiner withUser(StringValue user) {
        setUser(user);
        return this;
    }

    @Override
    public TriggerDefiner withHost(StringValue host) {
        setHost(host);
        return this;
    }
}
