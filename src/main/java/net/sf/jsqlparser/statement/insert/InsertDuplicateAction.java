/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * on duplicate key is one of:
 *
 * ON DUPLICATE KEY UPDATE NOTHING ON DUPLICATE KEY UPDATE { column_name = { expression | DEFAULT } | ( column_name [, ...] ) = [ ROW ] ( { expression | DEFAULT
 * } [, ...] ) | ( column_name [, ...] ) = ( sub-SELECT ) } [, ...] [ WHERE condition ]
 *
 * @author zhangconan
 */
public class InsertDuplicateAction implements Serializable {

    ConflictActionType conflictActionType;
    Expression whereExpression;
    private List<UpdateSet> updateSets;

    public InsertDuplicateAction(ConflictActionType conflictActionType) {
        this.conflictActionType = Objects.requireNonNull(conflictActionType, "The Conflict Action Type is mandatory and must not be Null.");
    }

    public List<UpdateSet> getUpdateSets() {
        return updateSets;
    }

    public void setUpdateSets(List<UpdateSet> updateSets) {
        this.updateSets = updateSets;
    }

    public InsertDuplicateAction withUpdateSets(List<UpdateSet> updateSets) {
        this.setUpdateSets(updateSets);
        return this;
    }

    public ConflictActionType getConflictActionType() {
        return conflictActionType;
    }

    public void setConflictActionType(ConflictActionType conflictActionType) {
        this.conflictActionType = Objects.requireNonNull(conflictActionType, "The Conflict Action Type is mandatory and must not be Null.");
    }

    public InsertDuplicateAction withConflictActionType(ConflictActionType conflictActionType) {
        setConflictActionType(conflictActionType);
        return this;
    }

    public InsertDuplicateAction addUpdateSet(Column column, Expression expression) {
        return this.addUpdateSet(new UpdateSet());
    }

    public InsertDuplicateAction addUpdateSet(UpdateSet updateSet) {
        if (updateSets == null) {
            updateSets = new ArrayList<>();
        }
        this.updateSets.add(updateSet);
        return this;
    }

    public InsertDuplicateAction withUpdateSets(Collection<UpdateSet> updateSets) {
        this.setUpdateSets(new ArrayList<>(updateSets));
        return this;
    }

    public Expression getWhereExpression() {
        return whereExpression;
    }

    public void setWhereExpression(Expression whereExpression) {
        this.whereExpression = whereExpression;
    }

    public InsertDuplicateAction withWhereExpression(Expression whereExpression) {
        setWhereExpression(whereExpression);
        return this;
    }

    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
    public StringBuilder appendTo(StringBuilder builder) {
        switch (conflictActionType) {
            case NOTHING:
                builder.append(" NOTHING ");
                break;
            default:
                UpdateSet.appendUpdateSetsTo(builder, updateSets);

                if (whereExpression != null) {
                    builder.append(" WHERE ").append(whereExpression);
                }
                break;
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
