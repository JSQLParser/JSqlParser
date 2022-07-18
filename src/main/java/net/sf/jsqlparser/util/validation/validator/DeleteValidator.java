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
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class DeleteValidator extends AbstractValidator<Delete> {


    @Override
    public void validate(Delete delete) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.delete);

            validateOptionalFeature(c, delete.getTables(), Feature.deleteTables);
            validateOptionalFeature(c, delete.getJoins(), Feature.deleteJoin);
            validateOptionalFeature(c, delete.getLimit(), Feature.deleteLimit);
            validateOptionalFeature(c, delete.getOrderByElements(), Feature.deleteOrderBy);
            validateOptionalFeature(c, delete.getReturningExpressionList(), Feature.insertReturningExpressionList);
        }

        SelectValidator v = getValidator(SelectValidator.class);
        delete.getTable().accept(v);

        if (isNotEmpty(delete.getTables())) {
            delete.getTables().forEach(t -> t.accept(v));
        }

        validateOptionalExpression(delete.getWhere());
        validateOptionalOrderByElements(delete.getOrderByElements());

        v.validateOptionalJoins(delete.getJoins());

        if (delete.getLimit() != null) {
            getValidator(LimitValidator.class).validate(delete.getLimit());
        }

        if (isNotEmpty(delete.getReturningExpressionList())) {
            delete.getReturningExpressionList().forEach(c -> c .accept(v));
        }

    }

}
