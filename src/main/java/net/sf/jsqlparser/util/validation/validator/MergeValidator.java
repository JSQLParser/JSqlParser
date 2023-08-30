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
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class MergeValidator extends AbstractValidator<Merge> {


    @Override
    public void validate(Merge merge) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.merge);
        }
        validateOptionalExpression(merge.getOnCondition());
        // validateOptionalExpression(merge.getFromItem());
        if (merge.getMergeInsert() != null) {
            validateOptionalExpressions(merge.getMergeInsert().getColumns());
            validateOptionalExpressions(merge.getMergeInsert().getValues());
        }
        if (merge.getMergeUpdate() != null) {
            for (UpdateSet updateSet : merge.getMergeUpdate().getUpdateSets()) {
                validateOptionalExpressions(updateSet.getColumns());
                validateOptionalExpressions(updateSet.getValues());
            }
            validateOptionalExpression(merge.getMergeUpdate().getDeleteWhereCondition());
            validateOptionalExpression(merge.getMergeUpdate().getWhereCondition());
        }
        validateOptionalFromItems(merge.getFromItem());
    }

}
