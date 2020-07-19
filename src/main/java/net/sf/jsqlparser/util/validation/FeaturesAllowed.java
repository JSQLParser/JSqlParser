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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import net.sf.jsqlparser.parser.feature.Feature;

public class FeaturesAllowed implements FeatureSetValidation {

    public static final FeaturesAllowed INSERT = new FeaturesAllowed(Feature.insert,
            Feature.insertWithMulivalue).unmodifyable();
    public static final FeaturesAllowed SELECT = new FeaturesAllowed(Feature.select).unmodifyable();
    public static final FeaturesAllowed UPDATE = new FeaturesAllowed(Feature.update).unmodifyable();
    public static final FeaturesAllowed DELETE = new FeaturesAllowed(Feature.delete).unmodifyable();

    private Set<Feature> features = new HashSet<>();

    public FeaturesAllowed(Feature... f) {
        add(f);
    }

    public FeaturesAllowed add(Feature... f) {
        Stream.of(f).forEach(features::add);
        return this;
    }

    private FeaturesAllowed unmodifyable() {
        this.features = Collections.unmodifiableSet(features);
        return this;
    }

    @Override
    public Set<Feature> getFeatures() {
        return features;
    }

}
