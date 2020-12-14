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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;

public class ValidationContext {

    private Collection<ValidationCapability> capabilities;
    private FeatureConfiguration configuration = new FeatureConfiguration();
    private Map<ContextKey, Object> contextMap = new HashMap<>();

    public ValidationContext put(ContextKey key, Object value) {
        contextMap.put(key, value);
        return this;
    }

    public <T> T get(ContextKey key, Class<T> type) {
        T t = getOptional(key, type);
        if (t == null) {
            throw new IllegalStateException(key + ": value missing");
        }
        return t;
    }

    public <T> T getOptional(ContextKey key, Class<T> type) {
        return type.cast(contextMap.get(key));
    }

    public ValidationContext reinit(boolean reInit) {
        if (reInit) {
            contextMap.clear();
        }
        return this;
    }

    public ValidationContext setConfiguration(FeatureConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public FeatureConfiguration getConfiguration() {
        return configuration;
    }

    public boolean getAsBoolean(Feature f) {
        return getConfiguration().getAsBoolean(f);
    }

    public String getAsString(Feature f) {
        return getConfiguration().getAsString(f);
    }

    public Collection<ValidationCapability> getCapabilities() {
        return capabilities;
    }

    public ValidationContext setCapabilities(Collection<ValidationCapability> capabilities) {
        this.capabilities = capabilities;
        return this;
    }

}
