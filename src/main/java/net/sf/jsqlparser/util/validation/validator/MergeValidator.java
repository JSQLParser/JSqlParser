/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import java.util.function.Consumer;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.util.validation.feature.FeatureSetValidation;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.feature.FeatureContext;

public class MergeValidator extends AbstractValidator<Merge> {


    @Override
    public void validate(Merge merge) {
        for (ValidationCapability c : getCapabilities()) {
            Consumer<String> messageConsumer = getMessageConsumer(c);
            if (c instanceof FeatureSetValidation) {
                c.validate(context().put(FeatureContext.feature, Feature.merge), messageConsumer);
            }
        }
    }

}
