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
import net.sf.jsqlparser.util.validation.ValidationException;

public interface FeatureSetValidation extends ValidationCapability, FeatureSet {

    String DEFAULT_NAME = "feature set";

    @Override
    default void validate(ValidationContext context, Consumer<ValidationException> errorConsumer) {
        Feature feature = context.get(FeatureContext.feature, Feature.class);
        if (!contains(feature)) {
            errorConsumer.accept(getMessage(feature));
        }
    }

    /**
     * @return all supported {@link Feature}'s
     */
    @Override
    Set<Feature> getFeatures();

    /**
     * @return the default message if not contained in the feature set
     */
    ValidationException getMessage(Feature feature);

    @Override
    default String getName() {
        return DEFAULT_NAME;
    }
}
