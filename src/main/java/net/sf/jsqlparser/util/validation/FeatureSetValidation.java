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

import java.util.Set;
import java.util.function.Consumer;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.parser.feature.FeatureSet;

public interface FeatureSetValidation extends ValidationCapability, FeatureSet {

    public enum Keys implements ContextKey {
        /**
         * @see Feature
         */
        feature
    }

    /**
     * @param feature
     */
    @Override
    default void validate(ValidationContext ctx, Consumer<String> errorMessageConsumer) {
        Feature feature = ctx.get(Keys.feature, Feature.class);
        if (!getFeatures().contains(feature)) {
            errorMessageConsumer.accept(getNotSupportedMessage(feature));
        } else if (FeatureConfiguration.getInstance().isDisabled(feature)) {
            errorMessageConsumer.accept(getDisabledMessage(feature));
        }
    }

    /**
     * @return all supported {@link Feature}'s
     */
    @Override
    public Set<Feature> getFeatures();

    /**
     * @return <code>featureName + " not supported."</code>
     */
    default String getNotSupportedMessage(Feature feature) {
        return feature.name() + " not supported.";
    }

    /**
     * @return <code>featureName + " not supported."</code>
     */
    default String getDisabledMessage(Feature feature) {
        return feature.name() + " is disabled.";
    }


}
