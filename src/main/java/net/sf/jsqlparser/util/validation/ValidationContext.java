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
        return type.cast(contextMap.get(key));
    }

    public ValidationContext reinit(boolean reInit) {
        if (reInit) {
            contextMap.clear();
        }
        return this;
    }

    public void setConfiguration(FeatureConfiguration configuration) {
        this.configuration = configuration;
    }

    public FeatureConfiguration getConfiguration() {
        return configuration;
    }

    public boolean isEnabled(Feature f) {
        return getConfiguration().isEnabled(f);
    }

    public boolean isDisabled(Feature f) {
        return getConfiguration().isDisabled(f);
    }

    public Collection<ValidationCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Collection<ValidationCapability> capabilities) {
        this.capabilities = capabilities;
    }

}
