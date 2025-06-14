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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class MergeInsert implements Serializable, MergeOperation {

    private Expression andPredicate;
    private ExpressionList<Column> columns;
    private ExpressionList<Expression> values;
    private Expression whereCondition;

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

    @Override
    public <S, T> T accept(MergeOperationVisitor<T> mergeOperationVisitor, S context) {
        return mergeOperationVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(" WHEN NOT MATCHED");
        if (andPredicate != null) {
            b.append(" AND ").append(andPredicate);
        }
        b.append(" THEN INSERT ");
        if (columns != null) {
            b.append(columns);
        }
        b.append(" VALUES ").append(values.toString());
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

    public <E extends Expression> E getAndPredicate(Class<E> type) {
        return type.cast(getAndPredicate());
    }

    public <E extends Expression> E getWhereCondition(Class<E> type) {
        return type.cast(getWhereCondition());
    }
}
