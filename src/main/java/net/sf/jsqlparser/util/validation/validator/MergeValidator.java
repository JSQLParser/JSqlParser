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
public class MergeValidator<Void> extends AbstractValidator<Merge>
        implements MergeOperationVisitor<Void> {


    @Override
    public void validate(Merge merge) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.merge);
        }
        validateOptionalExpression(merge.getOnCondition());
        if (merge.getOperations() != null) {
            merge.getOperations().forEach(operation -> operation.accept(this, null));
        }
        validateOptionalFromItems(merge.getFromItem());
    }

    @Override
    public <S> Void visit(MergeDelete mergeDelete, S context) {
        validateOptionalExpression(mergeDelete.getAndPredicate());
        return null;
    }

    public void visit(MergeDelete mergeDelete) {
        visit(mergeDelete, null);
    }

    @Override
    public <S> Void visit(MergeUpdate mergeUpdate, S context) {
        validateOptionalExpression(mergeUpdate.getAndPredicate());
        for (UpdateSet updateSet : mergeUpdate.getUpdateSets()) {
            validateOptionalExpressions(updateSet.getColumns());
            validateOptionalExpressions(updateSet.getValues());
        }
        validateOptionalExpression(mergeUpdate.getDeleteWhereCondition());
        validateOptionalExpression(mergeUpdate.getWhereCondition());
        return null;
    }

    public void visit(MergeUpdate mergeUpdate) {
        visit(mergeUpdate, null);
    }

    @Override
    public <S> Void visit(MergeInsert mergeInsert, S context) {
        validateOptionalExpression(mergeInsert.getAndPredicate());
        validateOptionalExpressions(mergeInsert.getColumns());
        validateOptionalExpressions(mergeInsert.getValues());

        return null;
    }

    public void visit(MergeInsert mergeInsert) {
        visit(mergeInsert, null);
    }
}
