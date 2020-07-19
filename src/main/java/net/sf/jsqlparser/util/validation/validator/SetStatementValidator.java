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
        }
        //        buffer.append("SET ");
        //
        //        for (int i = 0; i < set.getCount(); i++) {
        //            if (i > 0) {
        //                buffer.append(", ");
        //            }
        //            buffer.append(set.getName(i));
        //            if (set.isUseEqual(i)) {
        //                buffer.append(" =");
        //            }
        //            buffer.append(" ");
        //            set.getExpression(i).accept(expressionVisitor);
        //        }

    }

}
