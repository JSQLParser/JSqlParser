/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.feature;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureConfiguration {

    private static final Logger LOG = Logger.getLogger(FeatureConfiguration.class.getName());

    private Map<Feature, Object> featureEnabled = new EnumMap<>(Feature.class);

    public FeatureConfiguration() {
        // set default-value for all switchable features
        EnumSet.allOf(Feature.class).stream().filter(Feature::isConfigurable)
        .forEach(f -> setValue(f, f.getDefaultValue()));
    }

    /**
     * @param feature
     * @param value
     * @return <code>this</code>
     */
    public FeatureConfiguration setValue(Feature feature, Object value) {
        if (feature.isConfigurable()) {
            featureEnabled.put(feature, value);
        } else {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning(feature.name() + " is not switchable - cannot set enabled = " + value);
            }
        }
        return this;
    }

    /**
     * @param feature
     * @return the configured feature value - can be <code>null</code>
     * @throws IllegalStateException - if given {@link Feature#isConfigurable()} ==
     *                               false
     */
    public Object getValue(Feature feature) {
        if (feature.isConfigurable()) {
            return featureEnabled.get(feature);
        } else {
            throw new IllegalStateException("The feature " + feature + " is not configurable!");
        }
    }

    public boolean getAsBoolean(Feature f) {
        return Boolean.parseBoolean(String.valueOf(getValue(f)));
    }

    public Long getAsLong(Feature f) {
        return Long.valueOf(String.valueOf(getValue(f)));
    }

    public String getAsString(Feature f) {
        Object value = getValue(f);
        return value == null ? null : String.valueOf(value);
    }

}
