/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.feature;

import java.util.Set;
import java.util.function.Consumer;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureSet;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationContext;

public interface FeatureSetValidation extends ValidationCapability, FeatureSet {

    /**
     * @param feature
     */
    @Override
    default void validate(ValidationContext ctx, Consumer<String> errorMessageConsumer) {
        Feature feature = ctx.get(FeatureContext.feature, Feature.class);
        if (!contains(feature)) {
            errorMessageConsumer.accept(getMessage(feature));
        }
    }

    /**
     * @return all supported {@link Feature}'s
     */
    @Override
    public Set<Feature> getFeatures();

    /**
     * @return the default message if not contained in the feature set
     */
    public String getMessage(Feature feature);

    @Override
    default String getName() {
        return "feature set";
    }


}
