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
import net.sf.jsqlparser.statement.select.PlainSelect;

public class MergeInsert {

    private List<Column> columns = null;
    private List<Expression> values = null;
    private Expression whereCondition;

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

    @Override
    public String toString() {
        return " WHEN NOT MATCHED THEN INSERT "
                + (columns.isEmpty() ? "" : PlainSelect.getStringList(columns, true, true))
                + " VALUES " + PlainSelect.getStringList(values, true, true)
                + ( whereCondition != null 
                        ? " WHERE " + whereCondition
                        : "" );
    }

    public MergeInsert withColumns(List<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public MergeInsert withValues(List<Expression> values) {
        this.setValues(values);
        return this;
    }

    public MergeInsert addColumns(Column... columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public MergeInsert addColumns(Collection<? extends Column> columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public MergeInsert addValues(Expression... values) {
        List<Expression> collection = Optional.ofNullable(getValues()).orElseGet(ArrayList::new);
        Collections.addAll(collection, values);
        return this.withValues(collection);
    }

    public MergeInsert addValues(Collection<? extends Expression> values) {
        List<Expression> collection = Optional.ofNullable(getValues()).orElseGet(ArrayList::new);
        collection.addAll(values);
        return this.withValues(collection);
    }
    
     public MergeInsert withWhereCondition(Expression whereCondition) {
        this.setWhereCondition(whereCondition);
        return this;
    }
     
     public <E extends Expression> E getWhereCondition(Class<E> type) {
        return type.cast(getWhereCondition());
    }
}
