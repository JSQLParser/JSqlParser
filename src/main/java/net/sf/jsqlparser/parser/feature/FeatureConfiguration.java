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

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureConfiguration {

    private static final Logger LOG = Logger.getLogger(FeatureConfiguration.class.getName());

    private Map<Feature, Boolean> featureEnabled = new EnumMap<>(Feature.class);

    public FeatureConfiguration() {
        // set default-value for all switchable features
        EnumSet.allOf(Feature.class).stream().filter(Feature::isSwitchable)
        .forEach(f -> setEnabled(f, f.isParserEnabled()));
    }

    /**
     * @param features
     * @param enabled
     * @return <code>this</code>
     */
    public FeatureConfiguration setEnabled(Collection<Feature> features, boolean enabled) {
        features.forEach(f -> setEnabled(f, enabled));
        return this;
    }

    /**
     * @param feature
     * @param enabled
     * @return <code>this</code>
     */
    public FeatureConfiguration setEnabled(Feature feature, boolean enabled) {
        if (feature.isSwitchable()) {
            featureEnabled.put(feature, enabled);
        } else {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning(feature.name() + " is not switchable - cannot set enabled = " + enabled);
            }
        }
        return this;
    }

    public boolean isEnabled(Feature feature) {
        if (feature.isSwitchable()) {
            return featureEnabled.get(feature);
        } else {
            return feature.isParserEnabled();
        }
    }

    public boolean isDisabled(Feature feature) {
        return !isEnabled(feature);
    }


}
