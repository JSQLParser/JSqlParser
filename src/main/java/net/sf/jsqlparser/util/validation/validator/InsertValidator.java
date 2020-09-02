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
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class InsertValidator extends AbstractValidator<Insert> {


    @Override
    public void validate(Insert insert) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.insert);
            validateOptionalFeature(c, insert.getItemsList(), Feature.insertValues);
            validateOptionalFeature(c, insert.getModifierPriority(), Feature.insertModifierPriority);
            validateFeature(c, insert.isModifierIgnore(), Feature.insertModifierIgnore);
            validateOptionalFeature(c, insert.getSelect(), Feature.insertFromSelect);
            validateFeature(c, insert.isUseSet(), Feature.insertUseSet);
            validateFeature(c, insert.isUseDuplicate(), Feature.insertUseDuplicateKeyUpdate);
            validateFeature(c, insert.isReturningAllColumns(), Feature.insertReturningAll);
            validateOptionalFeature(c, insert.getReturningExpressionList(), Feature.insertReturningExpressionList);
        }

        validateOptionalFromItem(insert.getTable());
        validateOptionalExpressions(insert.getColumns());
        validateOptionalItemsList(insert.getItemsList());

        if (insert.getSelect() != null) {
            insert.getSelect().accept(getValidator(StatementValidator.class));
        }

        if (insert.isUseSet()) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            // TODO is this useful?
            // validateModelCondition (insert.getSetColumns().size() !=
            // insert.getSetExpressionList().size(), "model-error");
            insert.getSetColumns().forEach(c -> c.accept(v));
            insert.getSetExpressionList().forEach(c -> c.accept(v));
        }

        if (insert.isUseDuplicate()) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            // TODO is this useful?
            // validateModelCondition (insert.getDuplicateUpdateColumns().size() !=
            // insert.getDuplicateUpdateExpressionList().size(), "model-error");
            insert.getDuplicateUpdateColumns().forEach(c -> c.accept(v));
            insert.getDuplicateUpdateExpressionList().forEach(c -> c.accept(v));
        }

        if (insert.getReturningExpressionList() != null) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            insert.getReturningExpressionList().forEach(c -> c.getExpression().accept(v));
        }
    }

}
