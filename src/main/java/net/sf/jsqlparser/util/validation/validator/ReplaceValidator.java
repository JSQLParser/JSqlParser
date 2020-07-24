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

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class ReplaceValidator extends AbstractValidator<Replace> {


    @Override
    public void validate(Replace replace) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.replace);
        }
        validateOptionalFromItem(replace.getTable());
        validateOptionalItemsList(replace.getItemsList());
        validateOptionalExpressions(replace.getExpressions());
        validateOptionalColumns(replace.getColumns());
    }

}
