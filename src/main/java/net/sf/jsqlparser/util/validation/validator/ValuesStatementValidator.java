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
import net.sf.jsqlparser.statement.values.ValuesStatement;

/**
 * @author gitmotte
 */
public class ValuesStatementValidator extends AbstractValidator<ValuesStatement> {

    @Override
    public void validate(ValuesStatement values) {
        validateFeature(Feature.values);
        validateOptionalExpressions(values.getExpressions());
    }
}
