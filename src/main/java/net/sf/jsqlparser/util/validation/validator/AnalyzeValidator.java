/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

public class AnalyzeValidator  extends AbstractValidator<Analyze>{
    @Override
    public void validate(Analyze analyze) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.analyze);
            validateName(c, NamedObject.table, analyze.getTable().getFullyQualifiedName(), true);
        }
    }
}
