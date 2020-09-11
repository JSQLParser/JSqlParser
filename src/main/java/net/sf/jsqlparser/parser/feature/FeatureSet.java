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

import java.util.HashSet;
import java.util.Set;

public interface FeatureSet {

    public Set<Feature> getFeatures();

    /**
     * @return <code>true</code> if the feature is identical to one of the features
     *         contained in this set, <code>false</code> otherwise
     */
    default boolean contains(Feature feature) {
        return getFeatures().contains(feature);
    }

    /**
     * @return a new {@link HashSet} with a copy of supported features
     */
    default Set<Feature> getFeaturesClone() {
        return new HashSet<>(getFeatures());
    }

    /**
     * @param features
     * @return all features within this feature set which are not contained in given
     *         set
     */
    default Set<Feature> getNotContained(Set<Feature> features) {
        Set<Feature> f = new HashSet<>(getFeatures());
        f.removeAll(features);
        return f;
    }

    /**
     * @param features
     * @return all features within this feature set which are contained in given
     *         set too.
     */
    default Set<Feature> retainAll(Set<Feature> features) {
        Set<Feature> f = new HashSet<>(getFeatures());
        f.retainAll(features);
        return f;
    }
}
