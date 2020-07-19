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
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class UseStatementValidator extends AbstractValidator<UseStatement> {

    @Override
    public void validate(UseStatement set) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.use);
        }
        //        buffer.append("USE ").append(set.getName());
    }
}
