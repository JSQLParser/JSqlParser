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
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class UpsertValidator extends AbstractValidator<Upsert> {

    @Override
    public void validate(Upsert upsert) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.upsert);
        }
        validateOptionalFromItem(upsert.getTable());
        validateOptionalExpressions(upsert.getColumns());
        validateOptionalItemsList(upsert.getItemsList());
        validateOptionalSelect(upsert.getSelect());
        if (upsert.isUseDuplicate()) {
            validateDuplicate(upsert);
        }
    }

    private void validateOptionalSelect(Select select) {
        if (select != null) {
            SelectValidator v = getValidator(SelectValidator.class);
            select.getSelectBody().accept(v);
        }
    }

    private void validateDuplicate(Upsert upsert) {
        validateOptionalExpressions(upsert.getDuplicateUpdateColumns());
        validateOptionalExpressions(upsert.getDuplicateUpdateExpressionList());
    }

}
