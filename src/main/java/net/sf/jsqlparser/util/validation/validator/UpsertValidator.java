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
        upsert.getTable().accept(getValidator(SelectValidator.class));

        validateOptionalColumns(upsert.getColumns());

        if (upsert.getItemsList() != null) {
            upsert.getItemsList().accept(getValidator(ItemListValidator.class));
        }

        if (upsert.getSelect() != null) {
            validateSelect(upsert);
        }

        if (upsert.isUseDuplicate()) {
            validateDuplicate(upsert);
        }
    }

    private void validateSelect(Upsert upsert) {
        SelectValidator v = getValidator(SelectValidator.class);
        if (upsert.getSelect().getWithItemsList() != null) {
            upsert.getSelect().getWithItemsList().forEach(with -> with.accept(v));
        }
        upsert.getSelect().getSelectBody().accept(v);
    }

    private void validateDuplicate(Upsert upsert) {
        validateOptionalColumns(upsert.getDuplicateUpdateColumns());
        validateOptionalExpressions(upsert.getDuplicateUpdateExpressionList());
    }

}
