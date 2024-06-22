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
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class InsertValidator extends AbstractValidator<Insert> {


    @Override
    public void validate(Insert insert) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.insert);

            if (insert.getSelect() instanceof Values) {
                validateOptionalFeature(c, insert.getSelect().as(Values.class),
                        Feature.insertValues);
            }

            validateOptionalFeature(c, insert.getModifierPriority(),
                    Feature.insertModifierPriority);
            validateFeature(c, insert.isModifierIgnore(), Feature.insertModifierIgnore);
            validateOptionalFeature(c, insert.getSelect(), Feature.insertFromSelect);
            validateFeature(c, insert.isUseSet(), Feature.insertUseSet);
            validateFeature(c, insert.isUseDuplicate(), Feature.insertUseDuplicateKeyUpdate);
            validateOptionalFeature(c, insert.getReturningClause(),
                    Feature.insertReturningExpressionList);
        }

        validateOptionalFromItem(insert.getTable());
        validateOptionalExpressions(insert.getColumns());

        if (insert.getSelect() instanceof Values) {
            insert.getSelect().accept(getValidator(StatementValidator.class));
            validateOptionalExpressions(insert.getValues().getExpressions());
        }

        if (insert.getSetUpdateSets() != null) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            // TODO is this useful?
            // validateModelCondition (insert.getSetColumns().size() !=
            // insert.getSetExpressionList().size(), "model-error");
            for (UpdateSet updateSet : insert.getSetUpdateSets()) {
                updateSet.getColumns().forEach(c -> c.accept(v, null));
                updateSet.getValues().forEach(c -> c.accept(v, null));
            }
        }

        if (insert.getDuplicateUpdateSets() != null) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            // TODO is this useful?
            // validateModelCondition (insert.getSetColumns().size() !=
            // insert.getSetExpressionList().size(), "model-error");
            for (UpdateSet updateSet : insert.getDuplicateUpdateSets()) {
                updateSet.getColumns().forEach(c -> c.accept(v, null));
                updateSet.getValues().forEach(c -> c.accept(v, null));
            }
        }

        if (insert.getReturningClause() != null) {
            SelectValidator v = getValidator(SelectValidator.class);
            insert.getReturningClause().forEach(c -> c.accept(v, null));
        }
    }

}
