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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class MergeUpdate {

    private List<Column> columns = null;
    private List<Expression> values = null;
    private Expression whereCondition;
    private Expression deleteWhereCondition;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Expression> getValues() {
        return values;
    }

    public void setValues(List<Expression> values) {
        this.values = values;
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
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(" WHEN MATCHED THEN UPDATE SET ");
        for (int i = 0; i < columns.size(); i++) {
            if (i != 0) {
                b.append(", ");
            }
            b.append(columns.get(i).toString()).append(" = ").append(values.get(i).toString());
        }
        if (whereCondition != null) {
            b.append(" WHERE ").append(whereCondition.toString());
        }
        if (deleteWhereCondition != null) {
            b.append(" DELETE WHERE ").append(deleteWhereCondition.toString());
        }
        return b.toString();
    }

    public MergeUpdate withColumns(List<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public MergeUpdate withValues(List<Expression> values) {
        this.setValues(values);
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

    public MergeUpdate addColumns(Column... columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public MergeUpdate addColumns(Collection<? extends Column> columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public MergeUpdate addValues(Expression... values) {
        List<Expression> collection = Optional.ofNullable(getValues()).orElseGet(ArrayList::new);
        Collections.addAll(collection, values);
        return this.withValues(collection);
    }

    public MergeUpdate addValues(Collection<? extends Expression> values) {
        List<Expression> collection = Optional.ofNullable(getValues()).orElseGet(ArrayList::new);
        collection.addAll(values);
        return this.withValues(collection);
    }

    public <E extends Expression> E getWhereCondition(Class<E> type) {
        return type.cast(getWhereCondition());
    }

    public <E extends Expression> E getDeleteWhereCondition(Class<E> type) {
        return type.cast(getDeleteWhereCondition());
    }
}
