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

import java.util.stream.Collectors;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class UpdateValidator extends AbstractValidator<Update> {

    @Override
    public void validate(Update update) {

        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.update);
            if (update.getFromItem() != null) {
                validateFeature(c, Feature.updateFrom);
            }
            if (update.getStartJoins() != null) {
                validateFeature(c, Feature.updateJoins);
            }
            if (update.isUseSelect()) {
                validateFeature(c, Feature.updateUseSelect);
            }
            if (update.getOrderByElements() != null) {
                validateFeature(c, Feature.updateOrderBy);
            }
            if (update.getLimit() != null) {
                validateFeature(c, Feature.updateLimit);
            }
            if (update.getReturningExpressionList() != null || update.isReturningAllColumns()) {
                validateFeature(c, Feature.updateReturning);
            }
        }

        validateOptionalFromItem(update.getTable());

        validateOptional(update.getStartJoins(),
                j -> getValidator(SelectValidator.class).validateOptionalJoins(j));

        if (update.isUseSelect()) {
            validateOptionalExpressions(update.getColumns());
            validateOptional(update.getSelect(), e -> e.getSelectBody().accept(getValidator(SelectValidator.class)));
        } else {
            validateOptionalExpressions(update.getColumns());
            validateOptionalExpressions(update.getExpressions());
        }

        if (update.getFromItem() != null) {
            validateOptionalFromItem(update.getFromItem());
            validateOptional(update.getJoins(),
                    j -> getValidator(SelectValidator.class).validateOptionalJoins(j));
        }

        validateOptionalExpression(update.getWhere());
        validateOptionalOrderByElements(update.getOrderByElements());

        if (update.getLimit() != null) {
            getValidator(LimitValidator.class).validate(update.getLimit());
        }

        if (update.getReturningExpressionList() != null) {
            validateOptionalExpressions(update.getReturningExpressionList().stream()
                    .map(SelectExpressionItem::getExpression).collect(Collectors.toList()));
        }
    }


}
