/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.merge;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.io.Serializable;
import java.util.List;

public class MergeUpdate implements Serializable, MergeOperation {

    private List<UpdateSet> updateSets;
    private Expression andPredicate;
    private Expression whereCondition;
    private Expression deleteWhereCondition;

    public MergeUpdate() {}

    public MergeUpdate(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
    }

    public List<UpdateSet> getUpdateSets() {
        return updateSets;
    }

    public MergeUpdate setUpdateSets(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
        return this;
    }

    public Expression getAndPredicate() {
        return andPredicate;
    }

    public void setAndPredicate(Expression andPredicate) {
        this.andPredicate = andPredicate;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Expression whereCondition) {
        this.whereCondition = whereCondition;
    }

    public Expression getDeleteWhereCondition() {
        return deleteWhereCondition;
    }

    public void setDeleteWhereCondition(Expression deleteWhereCondition) {
        this.deleteWhereCondition = deleteWhereCondition;
    }

    @Override
    public void accept(MergeOperationVisitor mergeOperationVisitor) {
        mergeOperationVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(" WHEN MATCHED");
        if (andPredicate != null) {
            b.append(" AND ").append(andPredicate.toString());
        }
        b.append(" THEN UPDATE SET ");
        UpdateSet.appendUpdateSetsTo(b, updateSets);

        if (whereCondition != null) {
            b.append(" WHERE ").append(whereCondition.toString());
        }
        if (deleteWhereCondition != null) {
            b.append(" DELETE WHERE ").append(deleteWhereCondition.toString());
        }
        return b.toString();
    }

    public MergeUpdate withAndPredicate(Expression andPredicate) {
        this.setAndPredicate(andPredicate);
        return this;
    }

    public MergeUpdate withWhereCondition(Expression whereCondition) {
        this.setWhereCondition(whereCondition);
        return this;
    }

    public MergeUpdate withDeleteWhereCondition(Expression deleteWhereCondition) {
        this.setDeleteWhereCondition(deleteWhereCondition);
        return this;
    }

    public <E extends Expression> E getAndPredicate(Class<E> type) {
        return type.cast(getAndPredicate());
    }

    public <E extends Expression> E getWhereCondition(Class<E> type) {
        return type.cast(getWhereCondition());
    }

    public <E extends Expression> E getDeleteWhereCondition(Class<E> type) {
        return type.cast(getDeleteWhereCondition());
    }
}
