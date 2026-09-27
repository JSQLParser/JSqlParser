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
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class SetStatementValidator extends AbstractValidator<SetStatement> {


    @Override
    public void validate(SetStatement set) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.set);
            if (set.getOnOffOptions() != null) {
                validateFeature(c, Feature.sqlServerSetOptions);
            }
            for (int i = 0; i < set.getCount(); i++) {
                if (set.getAssignmentOperator(i) == SetStatement.AssignmentOperator.TO) {
                    validateFeature(c, Feature.setAssignmentTo);
                }
            }
        }
        for (int i = 0; i < set.getCount(); i++) {
            validateOptionalExpressions(set.getExpressions(i));
        }
    }

}
