/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
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
import java.util.Objects;

/**
 * https://www.postgresql.org/docs/current/sql-insert.html
 * <pre>
 * conflict_action is one of:
 *
 *     DO NOTHING
 *     DO UPDATE SET { column_name = { expression | DEFAULT } |
 *                     ( column_name [, ...] ) = [ ROW ] ( { expression | DEFAULT } [, ...] ) |
 *                     ( column_name [, ...] ) = ( sub-SELECT )
 *                   } [, ...]
 *               [ WHERE condition ]
 * </pre>
 */

public class InsertConflictAction implements Serializable {
    ConflictActionType conflictActionType;

    private final ArrayList<UpdateSet> updateSets = new ArrayList<>();
    Expression whereExpression;

    public InsertConflictAction(ConflictActionType conflictActionType) {
        this.conflictActionType = Objects.requireNonNull(conflictActionType, "The Conflict Action Type is mandatory and must not be Null.");
    }

    public ConflictActionType getConflictActionType() {
        return conflictActionType;
    }

    public void setConflictActionType(ConflictActionType conflictActionType) {
        this.conflictActionType = Objects.requireNonNull(conflictActionType, "The Conflict Action Type is mandatory and must not be Null.");
    }

    public InsertConflictAction withConflictActionType(ConflictActionType conflictActionType) {
        setConflictActionType(conflictActionType);
        return this;
    }

    public InsertConflictAction addUpdateSet(Column column, Expression expression) {
        this.updateSets.add(new UpdateSet(column, expression));
        return this;
    }

    public InsertConflictAction addUpdateSet(UpdateSet updateSet) {
        this.updateSets.add(updateSet);
        return this;
    }

    public InsertConflictAction withUpdateSets(Collection<UpdateSet> updateSets) {
        this.updateSets.clear();
        this.updateSets.addAll(updateSets);
        return this;
    }

    public Expression getWhereExpression() {
        return whereExpression;
    }

    public void setWhereExpression(Expression whereExpression) {
        this.whereExpression = whereExpression;
    }

    public InsertConflictAction withWhereExpression(Expression whereExpression) {
        setWhereExpression(whereExpression);
        return this;
    }

    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
    public StringBuilder appendTo(StringBuilder builder) {
        switch (conflictActionType) {
            case DO_NOTHING:
                builder.append(" DO NOTHING");
                break;
            case DO_UPDATE:
                builder.append(" DO UPDATE ");
                UpdateSet.appendUpdateSetsTo(builder, updateSets);

                if (whereExpression!=null) {
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
