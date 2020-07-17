/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import java.util.HashMap;
import java.util.Map;

public class ValidationContext {

    private Map<ContextKey, Object> contextMap = new HashMap<>();

    public ValidationContext put(ContextKey key, Object value) {
        contextMap.put(key, value);
        return this;
    }

    public <T> T get(ContextKey key, Class<T> type) {
        return type.cast(contextMap.get(key));
    }

    public ValidationContext reinit(boolean reInit) {
        if (reInit) {
            contextMap.clear();
        }
        return this;
    }
}
