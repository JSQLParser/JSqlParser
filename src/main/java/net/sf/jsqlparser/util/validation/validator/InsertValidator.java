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
            validateFeature(c, insert.getModifierPriority() != null, Feature.insertModifierPriority);
            validateFeature(c, insert.isModifierIgnore(), Feature.insertModifierIgnore);
            validateFeature(c, insert.getSelect() != null, Feature.insertFromSelect);
            validateFeature(c, insert.isUseSet(), Feature.insertUseSet);
            validateFeature(c, insert.isUseDuplicate(), Feature.insertUseDuplicateKeyUpdate);
            validateFeature(c, insert.isReturningAllColumns(), Feature.insertReturning);
            validateFeature(c, insert.getReturningExpressionList() != null, Feature.insertReturningExpressionList);
        }

        insert.getTable().accept(getValidator(SelectValidator.class));

        if (insert.getColumns() != null) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            insert.getColumns().forEach(c -> c.accept(v));
        }

        if (insert.getItemsList() != null) {
            insert.getItemsList().accept(getValidator(ItemListValidator.class));
        }

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
