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
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author jxnu-liguobin
 */
public class TableStatementValidator extends AbstractValidator<TableStatement> {

    @Override
    public void validate(TableStatement statement) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.tableStatement);
        }
    }
}
