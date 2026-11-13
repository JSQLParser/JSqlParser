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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class MergeInsert implements Serializable, MergeOperation {

    private Expression andPredicate;
    private ExpressionList<Column> columns;
    private ExpressionList<Expression> values;
    private Expression whereCondition;
    private MergeSide side;
    private Insert.OverridingMode overridingMode = Insert.OverridingMode.NONE;

    public MergeSide getSide() {
        return side;
    }

    public void setSide(MergeSide side) {
        this.side = side;
    }

    public Expression getAndPredicate() {
        return andPredicate;
    }

    public void setAndPredicate(Expression andPredicate) {
        this.andPredicate = andPredicate;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> columns) {
        this.columns = columns;
    }

    public ExpressionList<Expression> getValues() {
        return values;
    }

    public void setValues(ExpressionList<Expression> values) {
        this.values = values;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Expression whereCondition) {
        this.whereCondition = whereCondition;
    }

    /** {@code WHEN NOT MATCHED}, or {@code WHEN NOT MATCHED BY TARGET} with that side. */
    public MergeWhen getWhen() {
        return MergeWhen.ofSourceRow(side);
    }

    /** The mode of {@code OVERRIDING {SYSTEM | USER} VALUE}, {@code NONE} without the clause. */
    public Insert.OverridingMode getOverridingMode() {
        return overridingMode;
    }

    public void setOverridingMode(Insert.OverridingMode overridingMode) {
        this.overridingMode = Objects.requireNonNull(overridingMode);
    }

    public MergeInsert withOverridingMode(Insert.OverridingMode overridingMode) {
        this.setOverridingMode(overridingMode);
        return this;
    }

    /** Whether the clause is {@code INSERT DEFAULT VALUES}: an insert without a values list. */
    public boolean isDefaultValues() {
        return values == null;
    }

    /**
     * Appends {@code INSERT DEFAULT VALUES} or {@code INSERT [(columns)] [OVERRIDING ... VALUE]
     * VALUES (...)}, letting the renderer write the column and value lists.
     */
    public StringBuilder appendInsertTo(StringBuilder builder,
            Consumer<Expression> expressionRenderer) {
        builder.append("INSERT ");
        if (isDefaultValues()) {
            return builder.append("DEFAULT VALUES");
        }
        if (columns != null) {
            expressionRenderer.accept(columns);
        }
        if (overridingMode != Insert.OverridingMode.NONE) {
            builder.append(columns != null ? " " : "").append("OVERRIDING ")
                    .append(overridingMode).append(" VALUE");
        }
        builder.append(" VALUES ");
        expressionRenderer.accept(values);
        return builder;
    }

    @Override
    public <S, T> T accept(MergeOperationVisitor<T> mergeOperationVisitor, S context) {
        return mergeOperationVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        getWhen().appendTo(b, andPredicate, b::append);
        appendInsertTo(b, b::append);
        if (whereCondition != null) {
            b.append(" WHERE ").append(whereCondition);
        }
        return b.toString();
    }

    public MergeInsert withAndPredicate(Expression andPredicate) {
        this.setAndPredicate(andPredicate);
        return this;
    }

    public MergeInsert withColumns(ExpressionList<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public MergeInsert withValues(ExpressionList<Expression> values) {
        this.setValues(values);
        return this;
    }

    public MergeInsert addColumns(Column... columns) {
        return this.addColumns(Arrays.asList(columns));
    }

    public MergeInsert addColumns(Collection<? extends Column> columns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getColumns()).orElseGet(ExpressionList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public MergeInsert addValues(Expression... values) {
        return this.addValues(Arrays.asList(values));
    }

    public MergeInsert addValues(Collection<? extends Expression> values) {
        ExpressionList<Expression> collection =
                Optional.ofNullable(getValues()).orElseGet(ExpressionList::new);
        collection.addAll(values);
        return this.withValues(collection);
    }

    public MergeInsert withWhereCondition(Expression whereCondition) {
        this.setWhereCondition(whereCondition);
        return this;
    }

    public MergeInsert withSide(MergeSide side) {
        this.setSide(side);
        return this;
    }

    public <E extends Expression> E getAndPredicate(Class<E> type) {
        return type.cast(getAndPredicate());
    }

    public <E extends Expression> E getWhereCondition(Class<E> type) {
        return type.cast(getWhereCondition());
    }
}
