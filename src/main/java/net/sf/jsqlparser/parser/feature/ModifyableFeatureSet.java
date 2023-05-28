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

public interface ModifyableFeatureSet extends FeatureSet {

    /**
     * @param featureSets
     * @return <code>this</code>
     */
    ModifyableFeatureSet add(FeatureSet... featureSets);

    /**
     * @param features
     * @return <code>this</code>
     */
    ModifyableFeatureSet add(Feature... features);

    /**
     * @param features
     * @return <code>this</code>
     */
    ModifyableFeatureSet add(Collection<Feature> features);

    /**
     * @param featureSets
     * @return <code>this</code>
     */
    ModifyableFeatureSet remove(FeatureSet... featureSets);

    /**
     * @param features
     * @return <code>this</code>
     */
    ModifyableFeatureSet remove(Feature... features);

    /**
     * @param features
     * @return <code>this</code>
     */
    ModifyableFeatureSet remove(Collection<Feature> features);

    /**
     * makes the inner {@link Feature}-set unmodifiable
     *
     * @return <code>this</code>
     * @see #copy()
     */
    FeatureSet unmodifyable();
}
