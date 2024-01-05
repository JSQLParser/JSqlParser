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
import net.sf.jsqlparser.statement.merge.*;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class MergeValidator extends AbstractValidator<Merge> implements MergeOperationVisitor {


    @Override
    public void validate(Merge merge) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.merge);
        }
        validateOptionalExpression(merge.getOnCondition());
        if (merge.getOperations() != null) {
            merge.getOperations().forEach(operation -> operation.accept(this));
        }
        validateOptionalFromItems(merge.getFromItem());
    }

    @Override
    public void visit(MergeDelete mergeDelete) {
        validateOptionalExpression(mergeDelete.getAndPredicate());
    }

    @Override
    public void visit(MergeUpdate mergeUpdate) {
        validateOptionalExpression(mergeUpdate.getAndPredicate());
        for (UpdateSet updateSet : mergeUpdate.getUpdateSets()) {
            validateOptionalExpressions(updateSet.getColumns());
            validateOptionalExpressions(updateSet.getValues());
        }
        validateOptionalExpression(mergeUpdate.getDeleteWhereCondition());
        validateOptionalExpression(mergeUpdate.getWhereCondition());
    }

    @Override
    public void visit(MergeInsert mergeInsert) {
        validateOptionalExpression(mergeInsert.getAndPredicate());
        validateOptionalExpressions(mergeInsert.getColumns());
        validateOptionalExpressions(mergeInsert.getValues());
    }
}
